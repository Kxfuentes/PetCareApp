package com.proyectopoo.petcareapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.proyectopoo.petcareapp.ui.screen.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = Login,
        modifier = modifier
    ) {

        composable<Login> {
            LoginScreen(
                onRoleSelection = {
                    navController.navigate(RoleSection)
                },
                onGoToRegister = {
                    navController.navigate(Register)
                }
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(RoleSection)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<RoleSection> {
            RoleSectionScreen(
                onOwnerSelected = {
                    navController.navigate(DogInfo)
                },
                onCaregiverSelected = {
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
                onGoToProfile = { cuidadorId ->
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
                }
            )
        }
    }
}