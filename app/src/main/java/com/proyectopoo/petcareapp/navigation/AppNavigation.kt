package com.proyectopoo.petcareapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.proyectopoo.petcareapp.LocalUserRoleViewModel
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.data.repository.UserRepository
import com.proyectopoo.petcareapp.data.session.SessionManager
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.ui.screen.auth.LoginScreen
import com.proyectopoo.petcareapp.ui.screen.auth.PasswordRecoveryScreen
import com.proyectopoo.petcareapp.ui.screen.auth.RegisterScreen
import com.proyectopoo.petcareapp.ui.screen.auth.RoleSectionScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverFeedScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverHomeScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverProfileScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverServiceScreen
import com.proyectopoo.petcareapp.ui.screen.owner.CreateServiceScreen
import com.proyectopoo.petcareapp.ui.screen.owner.DogInfoScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerFeedScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerHomeScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerProfileScreen
import com.proyectopoo.petcareapp.viewmodel.DogViewModel
import com.proyectopoo.petcareapp.viewmodel.LoginViewModel
import com.proyectopoo.petcareapp.viewmodel.UserRoleViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sessionLogout: (NavHostController, UserRoleViewModel) -> Unit
) {
    val userRoleViewModel = LocalUserRoleViewModel.current
    val dogViewModel: DogViewModel = viewModel()

    // Colecciona los perros para poder pasarlos a OwnerHomeScreen
    val dogs by dogViewModel.dogs.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Login,
        modifier = modifier
    ) {
        composable<Login> {
            val context = LocalContext.current
            val database = PetCareDatabase.getDatabase(context)
            val sessionManager = SessionManager(context)

            val loginViewModel: LoginViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        LoginViewModel(
                            UserRepository(
                                userDao = database.userDao(),
                                sessionManager = sessionManager,
                                apiService = RetrofitClient.apiService
                            )
                        )
                    }
                }
            )

            val loggedUser by loginViewModel.loggedUser.collectAsStateWithLifecycle()
            val errorMessage by loginViewModel.errorMessage.collectAsStateWithLifecycle()
            val isLoading by loginViewModel.isLoading.collectAsStateWithLifecycle()

            LaunchedEffect(loggedUser) {
                loggedUser?.let { user ->
                    val role = when (user.role.name) {
                        "CAREGIVER" -> UserRole.CAREGIVER
                        else -> UserRole.OWNER
                    }

                    userRoleViewModel.setRole(role)

                    navController.navigate(
                        if (role == UserRole.CAREGIVER) CaregiverHome else OwnerHome
                    ) {
                        popUpTo(Login) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            LoginScreen(
                onLoginClick = { email, password, rememberSession ->
                    loginViewModel.login(
                        email = email,
                        password = password,
                        rememberSession = rememberSession
                    )
                },
                onGoToRegister = { navController.navigate(Register) },
                onGoToPasswordRecovery = {
                    navController.navigate(PasswordRecovery) {
                        launchSingleTop = true
                    }
                },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = { response, password ->
                    val userData = response.user ?: response.useer

                    if (userData != null) {
                        navController.navigate(
                            RoleSection(
                                userId = userData.id,
                                username = userData.username,
                                email = userData.email,
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

                    val destination = if (dogs.isEmpty()) {
                        DogInfo()
                    } else {
                        OwnerHome
                    }

                    navController.navigate(destination) {
                        popUpTo(Login) { inclusive = true }
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
            val editingDog = dogs.find { it.petId == args.petId }

            DogInfoScreen(
                editingDog = editingDog,
                onFinish = { name, breed, size ->
                    val isNewDog = args.petId == -1
                    val petId = if (isNewDog) {
                        (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                    } else {
                        args.petId
                    }

                    val pet = PetEntity(
                        petId = petId,
                        ownerId = 1, // TODO: obtener del session manager
                        name = name,
                        breed = breed,
                        size = size,
                        species = "Dog"
                    )

                    if (isNewDog) {
                        dogViewModel.addDog(pet)
                    } else {
                        dogViewModel.updateDog(pet)
                    }

                    navController.navigate(OwnerHome) {
                        popUpTo(navController.graph.id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ✅ Pantalla OwnerHome corregida
        composable<OwnerHome> {
            val ownerId = 1 // TODO: desde SessionManager

            OwnerHomeScreen(
                dogs = dogs,                              // ← se pasa la lista real
                onGoToCreate = { navController.navigate(CreateService) },
                onEditPets = { pet: PetEntity ->         // ← ahora recibe PetEntity
                    navController.navigate(DogInfo(petId = pet.petId))
                },
                onAddDog = { navController.navigate(DogInfo()) },
                onGoToFeed = { navController.navigate(OwnerFeed) },
                onGoToOwnerProfile = { navController.navigate(OwnerProfile) },
                ownerId = ownerId
            )
        }

        composable<OwnerFeed> {
            OwnerFeedScreen(
                onGoToOwnerProfile = { navController.navigate(OwnerProfile) }
            )
        }

        composable<CreateService> {
            CreateServiceScreen(
                onBack = { navController.popBackStack() },
                onPublish = {
                    navController.navigate(OwnerHome) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // ✅ Pantalla CaregiverHome corregida
        composable<CaregiverHome> {
            val caregiverId = 1 // TODO: desde SessionManager

            CaregiverHomeScreen(
                onGoToFeed = { navController.navigate(CaregiverFeed) },
                onGoToCreate = { navController.navigate(CreateService) },   // ← se añade
                onGoToServices = { navController.navigate(CaregiverService) },
                onGoToCaregiverProfile = { navController.navigate(CaregiverProfile) },
                caregiverId = caregiverId
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
                onLogout = { sessionLogout(navController, userRoleViewModel) }
            )
        }

        composable<CaregiverProfile> {
            CaregiverProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = { sessionLogout(navController, userRoleViewModel) }
            )
        }

        composable<PasswordRecovery> {
            PasswordRecoveryScreen(
                onBackToLogin = {
                    navController.navigate(Login) {
                        popUpTo(PasswordRecovery) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}