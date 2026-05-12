package com.proyectopoo.petcareapp.ui.navigation

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
import com.proyectopoo.petcareapp.LocalUserRoleViewModel
import com.proyectopoo.petcareapp.ui.screen.*

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
                onRoleSelection = {
                    if (userRole != null) {
                        when (userRole) {
                            UserRole.OWNER -> navController.navigate(OwnerHome)
                            UserRole.CAREGIVER -> navController.navigate(CaregiverHome)
                            else -> navController.navigate(RoleSection)
                        }
                    } else {
                        navController.navigate(RoleSection)
                    }
                },
                onGoToRegister = {
                    navController.navigate(Register)
                }
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = {
                    if (userRole != null) {
                        when (userRole) {
                            UserRole.OWNER -> navController.navigate(OwnerHome)
                            UserRole.CAREGIVER -> navController.navigate(CaregiverHome)
                            else -> navController.navigate(RoleSection)
                        }
                    } else {
                        navController.navigate(RoleSection)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<RoleSection> {
            RoleSectionScreen(
                onOwnerSelected = {
                    userRoleViewModel.setRole(UserRole.OWNER)
                    navController.navigate(DogInfo)
                },
                onCaregiverSelected = {
                    userRoleViewModel.setRole(UserRole.CAREGIVER)
                    navController.navigate(CaregiverHome)
                }
            )
        }

        composable<DogInfo> {
            DogInfoScreen(
                onFinish = {
                    navController.navigate(OwnerHome)
                }
            )
        }

        composable<OwnerHome> {
            OwnerHomeScreen(
                onGoToCreate = {
                    navController.navigate(CreateService)
                }
            )
        }

        composable<OwnerFeed> {
            OwnerFeedScreen(
                onGoToProfile = { _ ->
                    navController.navigate(Profile)
                }
            )
        }

        composable<CreateService> {
            CreateServiceScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<CaregiverHome> {
            CaregiverHomeScreen(
                onGoToProfile = {
                    navController.navigate(Profile)
                }
            )
        }

        composable<CaregiverFeed> {
            CaregiverFeedScreen(
                onGoToCreate = {
                    navController.navigate(CreateService)
                },
                onGoToProfile = {
                    navController.navigate(Profile)
                }
            )
        }

        composable<CaregiverServices> {
            CaregiverServiceScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Profile> {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
