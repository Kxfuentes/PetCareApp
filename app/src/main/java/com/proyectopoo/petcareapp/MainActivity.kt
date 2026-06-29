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
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.navigation.*
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

                    LaunchedEffect(Unit) {
                        handleAutoLogin(context, userRoleViewModel, navController)
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
                            sessionLogout = { nav, roleVM ->
                                val sessionManager = SessionManager(nav.context)
                                sessionManager.clearSession()
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