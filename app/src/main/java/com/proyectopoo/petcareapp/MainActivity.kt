package com.proyectopoo.petcareapp

/*
 * Comentario de modulo PetCare:
 * Archivo del proyecto PetCare. Mantiene una parte especifica de la app y debe conservarse simple de seguir.
 */

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType
import com.proyectopoo.petcareapp.data.session.SessionManager
import com.proyectopoo.petcareapp.data.websocket.PetCareWebSocketClient
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.navigation.AppNavigation
import com.proyectopoo.petcareapp.navigation.CaregiverHome
import com.proyectopoo.petcareapp.navigation.DogInfo
import com.proyectopoo.petcareapp.navigation.Login
import com.proyectopoo.petcareapp.navigation.OwnerHome
import com.proyectopoo.petcareapp.navigation.PasswordRecovery
import com.proyectopoo.petcareapp.navigation.Register
import com.proyectopoo.petcareapp.navigation.RequestOffer
import com.proyectopoo.petcareapp.navigation.RoleSection
import com.proyectopoo.petcareapp.ui.components.PetCareNavigationBar
import com.proyectopoo.petcareapp.ui.theme.PetCareAppTheme
import com.proyectopoo.petcareapp.viewmodel.UserRoleViewModel

val LocalUserRoleViewModel = compositionLocalOf<UserRoleViewModel> {
    error("No UserRoleViewModel provided")
}

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Si se deniega, las notificaciones solo se persisten en la base de datos. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)

        if (!shouldKeepSessionOpen()) {
            sessionManager.clearSession()
        }

        askNotificationPermission()

        setContent {
            PetCareAppTheme {
                val context = LocalContext.current

                val userRoleViewModel: UserRoleViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
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

                    val webSocketClient = remember {
                        PetCareWebSocketClient(
                            onEvent = { event ->
                                runOnUiThread {
                                    wsRefreshTick.intValue += 1
                                    Toast.makeText(
                                        this@MainActivity,
                                        event.message ?: "Nuevo evento de PetCare",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onStatusChanged = { connected ->
                                android.util.Log.d(
                                    "PetCareWebSocket",
                                    if (connected) "Conectado desde Android" else "Desconectado desde Android"
                                )
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
                                !dest.hasRoute<PasswordRecovery>()
                    } ?: false

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            if (showBottomBar) {
                                PetCareNavigationBar(
                                    navController = navController,
                                    userRole = userRole
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

                                val logoutSessionManager = SessionManager(nav.context)
                                logoutSessionManager.clearSession()
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

    private fun shouldKeepSessionOpen(): Boolean {
        return SessionManager(this).shouldRememberSession()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
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

        if (sessionManager.isLoggedIn() && shouldKeepSessionOpen()) {
            val savedRoleType = sessionManager.getRole()
            val savedRole = when (savedRoleType) {
                UserRoleType.CAREGIVER -> UserRole.CAREGIVER
                UserRoleType.OWNER -> UserRole.OWNER
                else -> null
            }

            savedRole?.let { role ->
                userRoleViewModel.setRole(role)

                val destination = if (role == UserRole.CAREGIVER) {
                    CaregiverHome
                } else {
                    OwnerHome
                }

                navController.navigate(destination) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}
