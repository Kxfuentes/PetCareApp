package com.proyectopoo.petcareapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.proyectopoo.petcareapp.navigation.AppNavigation
import com.proyectopoo.petcareapp.navigation.DogInfo
import com.proyectopoo.petcareapp.navigation.Login
import com.proyectopoo.petcareapp.navigation.Register
import com.proyectopoo.petcareapp.navigation.RoleSection
import com.proyectopoo.petcareapp.ui.components.PetCareNavigationBar
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.viewmodel.UserRoleViewModel
import com.proyectopoo.petcareapp.ui.theme.PetCareAppTheme
import androidx.compose.runtime.LaunchedEffect
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType
import com.proyectopoo.petcareapp.data.session.SessionManager
import com.proyectopoo.petcareapp.navigation.CaregiverHome
import com.proyectopoo.petcareapp.navigation.OwnerHome

val LocalUserRoleViewModel = compositionLocalOf<UserRoleViewModel> {
    error("No UserRoleViewModel provided")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                        val sessionManager = SessionManager(context)

                        if (sessionManager.isLoggedIn()) {
                            val savedRoleType = sessionManager.getRole()
                            val savedRole = when (savedRoleType) {
                                UserRoleType.CAREGIVER -> UserRole.CAREGIVER
                                UserRoleType.OWNER -> UserRole.OWNER
                                null -> null
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

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    val showBar = currentDestination?.let { dest ->
                        !dest.hasRoute<Login>() &&
                        !dest.hasRoute<Register>() &&
                        !dest.hasRoute<RoleSection>() &&
                        !dest.hasRoute<DogInfo>()
                    } ?: false

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (showBar) {
                                PetCareNavigationBar(
                                    navController = navController,
                                    userRole = userRole
                                )
                            }
                        }
                    ) { innerPadding ->
                        AppNavigation(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
