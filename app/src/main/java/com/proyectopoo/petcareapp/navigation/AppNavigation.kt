package com.proyectopoo.petcareapp.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.proyectopoo.petcareapp.LocalUserRoleViewModel
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.ui.screen.auth.*
import com.proyectopoo.petcareapp.ui.screen.caregiver.*
import com.proyectopoo.petcareapp.ui.screen.owner.*
import com.proyectopoo.petcareapp.viewmodel.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sessionLogout: (NavHostController, UserRoleViewModel) -> Unit
) {

    val userRoleViewModel = LocalUserRoleViewModel.current
    val dogViewModel: DogViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Login,
        modifier = modifier
    ) {

        composable<Login> {
            LoginScreen(
                onLoginClick = { _, _, _ -> },
                onGoToRegister = { navController.navigate(Register) },
                onGoToPasswordRecovery = { navController.navigate(PasswordRecovery) },
                isLoading = false,
                errorMessage = null
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = { _, _ ->
                    navController.navigate(RoleSection(0, "", "", ""))
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

                    if (dogViewModel.dogs.value.isEmpty()) {
                        navController.navigate(DogInfo(null)) {
                            popUpTo(Login) { inclusive = true }
                        }
                    } else {
                        navController.navigate(OwnerHome) {
                            popUpTo(Login) { inclusive = true }
                        }
                    }
                },

                onCaregiverSelected = {
                    userRoleViewModel.setRole(UserRole.CAREGIVER)

                    navController.navigate(CaregiverHome) {
                        popUpTo(Login) { inclusive = true }
                    }
                }
            )
        }

        composable<DogInfo> { backStackEntry ->

            val args = backStackEntry.toRoute<DogInfo>()
            val dogs by dogViewModel.dogs.collectAsStateWithLifecycle()

            val editingDog = remember(args.petId, dogs) {
                args.petId?.let { id ->
                    dogs.find { it.petId == id }
                }
            }

            DogInfoScreen(
                initialDog = editingDog,
                onFinish = { pet ->

                    if (editingDog == null) {
                        dogViewModel.addDog(pet)
                    } else {
                        dogViewModel.updateDog(pet)
                    }

                    navController.navigate(OwnerHome) {
                        popUpTo(DogInfo) { inclusive = true }
                    }
                }
            )
        }

        composable<OwnerHome> {

            val dogs by dogViewModel.dogs.collectAsStateWithLifecycle()

            OwnerHomeScreen(
                dogs = dogs,

                onAddDog = {
                    navController.navigate(DogInfo(null))
                },

                onGoToFeed = { navController.navigate(OwnerFeed) },

                onGoToCreate = { navController.navigate(CreateService) },

                onEditPets = { pet ->
                    navController.navigate(DogInfo(pet.petId))
                },

                onGoToOwnerProfile = { navController.navigate(OwnerProfile) }
            )
        }

        composable<OwnerProfile> {
            OwnerProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    sessionLogout(navController, userRoleViewModel)
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