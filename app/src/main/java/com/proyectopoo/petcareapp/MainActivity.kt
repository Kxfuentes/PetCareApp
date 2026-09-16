package com.proyectopoo.petcareapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.data.local.entity.NotificationType
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType
import com.proyectopoo.petcareapp.data.session.AppPreferences
import com.proyectopoo.petcareapp.data.session.SessionManager
import com.proyectopoo.petcareapp.data.websocket.PetCareWebSocketClient
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.navigation.*
import com.proyectopoo.petcareapp.notifications.AppNotifier
import com.proyectopoo.petcareapp.ui.components.ConnectivityBanner
import com.proyectopoo.petcareapp.ui.components.PetCareNavigationBar
import com.proyectopoo.petcareapp.ui.theme.PetCareAppTheme
import com.proyectopoo.petcareapp.viewmodel.UserRoleViewModel
import kotlinx.coroutines.launch

val LocalUserRoleViewModel = compositionLocalOf<UserRoleViewModel> {
    error("No UserRoleViewModel provided")
}

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Las notificaciones se persisten aunque no haya permiso de mostrar el aviso */ }

    // Deep link "petcare://solicitud/{id}" (ver AndroidManifest.xml y ShareUtils.kt). Un
    // `mutableStateOf` como campo de la Activity, no `remember`ado dentro de un Composable,
    // porque debe sobrevivir tanto al onCreate inicial como a onNewIntent (activity ya viva,
    // launchMode="singleTask") y seguir siendo observable por Compose en ambos casos.
    private var pendingDeepLinkIntent by mutableStateOf<Intent?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLinkIntent = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDeepLinkIntent = intent

        val sessionManager = SessionManager(this)

        // Limpiar sesión si no se debe recordar
        if (!sessionManager.shouldRememberSession()) {
            sessionManager.clearSession()
        }

        askNotificationPermission()

        setContent {
            val themeContext = LocalContext.current
            val appPreferences = remember { AppPreferences(themeContext) }
            var darkModeOverride by remember { mutableStateOf(appPreferences.getDarkModeOverride()) }
            val systemDarkTheme = isSystemInDarkTheme()

            PetCareAppTheme(darkTheme = darkModeOverride ?: systemDarkTheme) {
                val context = LocalContext.current

                // Usar el mismo SharedPreferences que SessionManager para consistencia si es necesario, 
                // o mantener "app_prefs" si UserRoleViewModel maneja UI state persistente aparte.
                val userRoleViewModel: UserRoleViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            val prefs = context.getSharedPreferences("petcare_session", Context.MODE_PRIVATE)
                            UserRoleViewModel(prefs)
                        }
                    }
                )

                val userRole by userRoleViewModel.userRole.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalUserRoleViewModel provides userRoleViewModel
                ) {
                    val navController = rememberNavController()
                    val wsRefreshTick = remember { mutableIntStateOf(0) }
                    val database = remember { PetCareDatabase.getDatabase(context) }
                    val notifier = remember { AppNotifier(context, database.notificationDao()) }
                    val notifierScope = rememberCoroutineScope()

                    val webSocketClient = remember {
                        PetCareWebSocketClient(
                            onEvent = { event ->
                                runOnUiThread { wsRefreshTick.intValue += 1 }
                                notifierScope.launch {
                                    notifier.push(
                                        recipientUserId = event.recipientUserId ?: event.userId ?: 0,
                                        title = event.title ?: "PetCare",
                                        message = event.message ?: "Nuevo evento de PetCare",
                                        type = notificationTypeFor(event.type)
                                    )
                                }
                            }
                        )
                    }

                    LaunchedEffect(Unit) {
                        handleAutoLogin(context, userRoleViewModel, navController)
                    }

                    // Deep link "petcare://solicitud/{id}": si hay sesion, deja el id pendiente
                    // en CalendarNavigationBridge (mismo mecanismo que "volver desde el
                    // calendario") y navega al Home del rol correspondiente, que abre el
                    // dialogo de detalle automaticamente. Sin sesion, se ignora en silencio y
                    // sigue el flujo normal de login.
                    LaunchedEffect(pendingDeepLinkIntent) {
                        val serviceRequestId = pendingDeepLinkIntent
                            ?.takeIf { it.action == Intent.ACTION_VIEW }
                            ?.data
                            ?.takeIf { it.scheme == "petcare" && it.host == "solicitud" }
                            ?.lastPathSegment
                            ?.toIntOrNull()

                        if (serviceRequestId != null && sessionManager.isLoggedIn()) {
                            val role = when (sessionManager.getRole()) {
                                UserRoleType.CAREGIVER -> UserRole.CAREGIVER
                                UserRoleType.OWNER -> UserRole.OWNER
                                else -> null
                            }
                            if (role != null) {
                                userRoleViewModel.setRole(role)
                                CalendarNavigationBridge.request(serviceRequestId)
                                val destination = if (role == UserRole.CAREGIVER) CaregiverHome else OwnerHome
                                navController.navigate(destination) { launchSingleTop = true }
                            }
                        }
                        pendingDeepLinkIntent = null
                    }

                    LaunchedEffect(userRole) {
                        val currentUserId = sessionManager.getUserId()
                        if (currentUserId > 0 && userRole != null) {
                            webSocketClient.connect(currentUserId)
                        } else {
                            webSocketClient.disconnect()
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            webSocketClient.disconnect()
                        }
                    }

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    val showBottomBar = currentDestination?.let { dest ->
                        !dest.hasRoute<Login>() &&
                                !dest.hasRoute<Register>() &&
                                !dest.hasRoute<RoleSection>() &&
                                !dest.hasRoute<RequestOffer>() &&
                                !dest.hasRoute<DogInfo>() &&
                                !dest.hasRoute<PasswordRecovery>() &&
                                !dest.hasRoute<Filtros>() &&
                                !dest.hasRoute<EditarSolicitud>() &&
                                !dest.hasRoute<Seguimiento>()
                    } ?: false

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            if (showBottomBar) {
                                PetCareNavigationBar(
                                    navController = navController,
                                    userRole = userRole,
                                    userId = sessionManager.getBackendUserId()
                                )
                            }
                        }
                    ) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {
                            ConnectivityBanner()
                            AppNavigation(
                            navController = navController,
                            modifier = Modifier.weight(1f),
                            wsRefreshTick = wsRefreshTick.intValue,
                            darkModeOverride = darkModeOverride,
                            onDarkModeOverrideChange = { value ->
                                darkModeOverride = value
                                appPreferences.setDarkModeOverride(value)
                            },
                            sessionLogout = { nav, roleVM ->
                                webSocketClient.disconnect()
                                val logoutSM = SessionManager(nav.context)
                                logoutSM.clearSession()
                                roleVM.clearRole()
                                nav.navigate(Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun handleAutoLogin(
        context: Context,
        userRoleViewModel: UserRoleViewModel,
        navController: NavHostController
    ) {
        val sessionManager = SessionManager(context)
        if (sessionManager.isLoggedIn()) {
            val savedRoleType = sessionManager.getRole()
            val savedRole = when (savedRoleType) {
                UserRoleType.CAREGIVER -> UserRole.CAREGIVER
                UserRoleType.OWNER -> UserRole.OWNER
                else -> null
            }

            savedRole?.let { role ->
                userRoleViewModel.setRole(role)
                val destination = if (role == UserRole.CAREGIVER) CaregiverHome else OwnerHome
                navController.navigate(destination) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}

private fun notificationTypeFor(eventType: String): NotificationType = when (eventType) {
    "SERVICE_REQUEST_CREATED", "APPLICATION_CREATED" -> NotificationType.SERVICE_REQUEST
    "APPLICATION_STATUS_UPDATED", "MY_APPLICATION_STATUS_UPDATED", "SERVICE_REQUEST_STATUS_UPDATED" -> NotificationType.REQUEST_ACCEPTED
    else -> NotificationType.GENERAL
}
