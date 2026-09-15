package com.proyectopoo.petcareapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.proyectopoo.petcareapp.data.session.SessionManager
import com.proyectopoo.petcareapp.data.websocket.PetCareWebSocketClient
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.navigation.*
import com.proyectopoo.petcareapp.notifications.AppNotifier
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)

        // Limpiar sesión si no se debe recordar
        if (!sessionManager.shouldRememberSession()) {
            sessionManager.clearSession()
        }

        askNotificationPermission()

        setContent {
            PetCareAppTheme {
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
                        AppNavigation(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding),
                            wsRefreshTick = wsRefreshTick.intValue,
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
