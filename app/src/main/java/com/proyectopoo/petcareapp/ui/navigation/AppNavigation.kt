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
                    val currentRole = userRoleViewModel.userRole.value
                    if (currentRole != null) {
                        when (currentRole) {
                            UserRole.OWNER -> navController.navigate(OwnerHome) {
                                popUpTo(Login) { inclusive = true }
                            }
                            UserRole.CAREGIVER -> navController.navigate(CaregiverHome) {
                                popUpTo(Login) { inclusive = true }
                            }
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
                    navController.navigate(RoleSection) {
                        popUpTo(Register) { inclusive = true }
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
                    navController.navigate(CaregiverHome) {
                        popUpTo(RoleSection) { inclusive = true }
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
                onGoToCreate = {
                    navController.navigate(CreateService)
                },
                onEditPets = {
                    navController.navigate(DogInfo)
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
                },
                onGoToServices = {
                    navController.navigate(CaregiverServices)
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
                    userRoleViewModel.clearRole()
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
