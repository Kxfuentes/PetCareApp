package com.proyectopoo.petcareapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.proyectopoo.petcareapp.LocalUserRoleViewModel
import com.proyectopoo.petcareapp.Viewmodel.UserRole
import com.proyectopoo.petcareapp.ui.screen.auth.LoginScreen
import com.proyectopoo.petcareapp.ui.screen.auth.PasswordRecoveryScreen
import com.proyectopoo.petcareapp.ui.screen.auth.RegisterScreen
import com.proyectopoo.petcareapp.ui.screen.auth.RoleSectionScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverFeedScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverHomeScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverServiceScreen
import com.proyectopoo.petcareapp.ui.screen.owner.CreateServiceScreen
import com.proyectopoo.petcareapp.ui.screen.owner.DogInfoScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerFeedScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerHomeScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverProfileScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerProfileScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val userRoleViewModel = LocalUserRoleViewModel.current
    val userRole by userRoleViewModel.userRole.collectAsStateWithLifecycle()
    val isRoleLoaded by userRoleViewModel.isRoleLoaded.collectAsStateWithLifecycle()

    if (!isRoleLoaded) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = when (userRole) {
        UserRole.OWNER -> OwnerHome
        UserRole.CAREGIVER -> CaregiverHome
        null -> Login
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {


        composable<Login> {
            LoginScreen(
                onRoleSelection = { /* Temporal */ },
                onGoToRegister = { navController.navigate(Register) },
                onGoToPasswordRecovery = { navController.navigate(PasswordRecovery) }
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = { response, password ->
                    val userData = response.user ?: response.useer
                    if (userData != null) {
                        navController.navigate(
                            RoleSection(
                                userId = userData.id ?: 0,
                                username = userData.username ?: "",
                                email = userData.email ?: "",
                                password = password
                            )
                        ) {
                            popUpTo(Register) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<RoleSection> { backStackEntry ->
            val data = backStackEntry.toRoute<RoleSection>()
            RoleSectionScreen(
                userId = data.userId,
                username = data.username,
                email = data.email,
                password = data.password,
                onOwnerSelected = {
                    userRoleViewModel.setRole(UserRole.OWNER)
                    navController.navigate(DogInfo)
                },
                onCaregiverSelected = {
                    userRoleViewModel.setRole(UserRole.CAREGIVER)
                    navController.navigate(CaregiverHome) {
                        popUpTo(data) { inclusive = true }  // Corregido
                    }
                }
            )
        }


        composable<DogInfo> {
            DogInfoScreen(
                onFinish = {
                    navController.navigate(OwnerHome) {
                        popUpTo(DogInfo) { inclusive = true }
                    }
                }
            )
        }

        composable<OwnerHome> {
            OwnerHomeScreen(
                onGoToFeed = { navController.navigate(OwnerFeed) },
                onGoToCreate = { navController.navigate(CreateService) },
                onEditPets = { navController.navigate(DogInfo) },
                onGoToOwnerProfile = { navController.navigate(OwnerProfile) }
            )
        }

        composable<OwnerFeed> {
            OwnerFeedScreen(
                onGoToOwnerProfile = { navController.navigate(OwnerProfile) }
            )
        }

        composable<CreateService> {
            CreateServiceScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<CaregiverHome> {
            CaregiverHomeScreen(
                onGoToFeed = { navController.navigate(CaregiverFeed) },
                onGoToCreate = { navController.navigate(CreateService) },
                onGoToServices = { navController.navigate(CaregiverService) },
                onGoToCaregiverProfile = { navController.navigate(CaregiverProfile) }
            )
        }

        composable<CaregiverFeed> {
            CaregiverFeedScreen(
                onGoToCreate = { navController.navigate(CreateService) },
                onGoToCaregiverProfile = { navController.navigate(CaregiverProfile) }
            )
        }

        composable<CaregiverService> {
            CaregiverServiceScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<OwnerProfile> {
            OwnerProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    userRoleViewModel.clearRole()
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<CaregiverProfile> {
            CaregiverProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    userRoleViewModel.clearRole()
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<PasswordRecovery> {
            PasswordRecoveryScreen(
                onBackToLogin = { navController.navigate(Login) }
            )
        }
    }
}