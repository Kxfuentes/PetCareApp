package com.proyectopoo.petcareapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.proyectopoo.petcareapp.ui.screen.CreateServiceScreen
import com.proyectopoo.petcareapp.ui.screen.FeedScreen
import com.proyectopoo.petcareapp.ui.screen.LoginScreen
import com.proyectopoo.petcareapp.ui.screen.ProfileScreen
import com.proyectopoo.petcareapp.ui.screen.RoleSectionScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Login
    ) {

        composable<Login> {
            LoginScreen(
                onGoToRoleSection = {
                    navController.navigate(RoleSection)
                }
            )
        }

        composable<RoleSection> {
            RoleSectionScreen(
                onGoToFeed = {
                    navController.navigate(Feed)
                }
            )
        }

        composable<Feed> {
            FeedScreen(
                onGoToCreateService = {
                    navController.navigate(CreateService)
                },
                onGoToProfile = {
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

        composable<Profile> {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

    }
}