package com.proyectopoo.petcareapp.ui.navigation

import com.proyectopoo.petcareapp.ui.screen.RoleSectionScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.proyectopoo.petcareapp.ui.screen.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
)
{
    NavHost(
        navController = navController,
        startDestination = Login,
        modifier = modifier
    )

    {
        composable<Login> {
            LoginScreen(
                onRoleSelection = { navController.navigate(RoleSection) },
                onGoToRegister = { navController.navigate(Register) }
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(RoleSection) },
                onBack = { navController.popBackStack() }
            )
        }

        composable<RoleSection> {

            RoleSectionScreen(

                onOwnerSelected = {
                    navController.navigate(DogInfo)
                },

                onCaregiverSelected = {
                    navController.navigate(Feed)
                }
            )
        }

        composable<Feed> {
            FeedScreen(
                onGoToCreate = { navController.navigate(CreateService) },
                onGoToProfile = { navController.navigate(Profile) }
            )
        }

        composable<CreateService> {
            CreateServiceScreen(onBack = { navController.popBackStack() })
        }

        composable<Profile> {
            ProfileScreen(onBack = { navController.popBackStack() })
        }

        composable<DogInfo> {

            DogInfoScreen(
                onFinish = {
                    navController.navigate(Feed)
                }
            )
        }
    }
}