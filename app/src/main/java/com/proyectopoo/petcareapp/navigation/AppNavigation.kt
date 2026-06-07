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
import com.proyectopoo.petcareapp.data.repository.ServiceApplicationRepository
import com.proyectopoo.petcareapp.data.repository.ServiceRequestRepository
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
import com.proyectopoo.petcareapp.viewmodel.ServiceRequestViewModel
import com.proyectopoo.petcareapp.viewmodel.UserRoleViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sessionLogout: (NavHostController, UserRoleViewModel) -> Unit
) {
    val userRoleViewModel = LocalUserRoleViewModel.current
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val dogViewModel: DogViewModel = viewModel()
    val database = PetCareDatabase.getDatabase(context)

    val serviceRequestViewModel: ServiceRequestViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ServiceRequestViewModel(
                    requestRepo = ServiceRequestRepository(database.serviceRequestDao()),
                    applicationRepo = ServiceApplicationRepository(database.serviceApplicationDao()),
                    userDao = database.userDao(),
                    ownerDao = database.ownerDao(),
                    caregiverDao = database.caregiverDao(),
                    petDao = database.petDao(),
                    serviceTypeDao = database.serviceTypeDao()
                )
            }
        }
    )

    val dogs by dogViewModel.dogs.collectAsStateWithLifecycle()
    val recentOwnerRequests by serviceRequestViewModel.recentOwnerRequests.collectAsStateWithLifecycle()
    val ownerApplicationDetails by serviceRequestViewModel.ownerApplicationDetails.collectAsStateWithLifecycle()
    val caregiverApplicationDetails by serviceRequestViewModel.caregiverApplicationDetails.collectAsStateWithLifecycle()
    val availableRequests by serviceRequestViewModel.availableRequests.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Login,
        modifier = modifier
    ) {
        composable<Login> {
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
                    loginViewModel.login(email, password, rememberSession)
                },
                onGoToRegister = { navController.navigate(Register) },
                onGoToPasswordRecovery = {
                    navController.navigate(PasswordRecovery) { launchSingleTop = true }
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
                    val destination = if (dogs.isEmpty()) DogInfo() else OwnerHome
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
            val ownerId = sessionManager.getUserId()

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
                        ownerId = ownerId,
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
                        popUpTo(navController.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<OwnerHome> {
            val ownerId = sessionManager.getUserId()
            LaunchedEffect(ownerId) {
                serviceRequestViewModel.loadOwnerData(ownerId)
            }
            OwnerHomeScreen(
                dogs = dogs,
                recentRequests = recentOwnerRequests,
                caregiverApplications = ownerApplicationDetails,
                onGoToCreate = { serviceType ->
                    navController.navigate(
                        CreateService(
                            serviceType = serviceType,
                            petName = dogs.firstOrNull()?.name ?: ""
                        )
                    )
                },
                onEditPets = { pet: PetEntity ->
                    navController.navigate(DogInfo(petId = pet.petId))
                },
                onDeletePet = { pet: PetEntity ->
                    dogViewModel.deleteDog(pet)
                },
                onAddDog = { navController.navigate(DogInfo()) },
                onGoToFeed = { navController.navigate(OwnerFeed) },
                onGoToOwnerProfile = { navController.navigate(OwnerProfile) },
                onAcceptApplication = { applicationId ->
                    serviceRequestViewModel.acceptApplication(applicationId, ownerId)
                },
                onRejectApplication = { applicationId ->
                    serviceRequestViewModel.rejectApplication(applicationId, ownerId)
                },
                ownerId = ownerId
            )
        }

        composable<OwnerFeed> {
            OwnerFeedScreen(
                onGoToCaregiverProfile = { caregiverId ->
                    navController.navigate(CaregiverProfile(caregiverId = caregiverId))
                }
            )
        }

        composable<CreateService> { backStackEntry ->
            val args = backStackEntry.toRoute<CreateService>()
            val ownerId = sessionManager.getUserId()
            CreateServiceScreen(
                petName = args.petName,
                serviceType = args.serviceType,
                onBack = { navController.popBackStack() },
                onPublish = { petName, serviceType, description, location, price, date ->
                    val existingPet = dogs.firstOrNull { it.name.equals(petName, ignoreCase = true) }
                    val petId = existingPet?.petId ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                    if (existingPet == null) {
                        dogViewModel.addDog(
                            PetEntity(
                                petId = petId,
                                ownerId = ownerId,
                                name = petName,
                                breed = null,
                                size = null,
                                species = "Dog"
                            )
                        )
                    }
                    serviceRequestViewModel.createRequestFromForm(
                        ownerId = ownerId,
                        petId = petId,
                        petName = petName,
                        serviceTypeName = serviceType,
                        description = description,
                        location = location,
                        price = price,
                        requestedDate = date
                    )
                    navController.navigate(OwnerHome) { launchSingleTop = true }
                },
                existingServices = recentOwnerRequests.mapNotNull { it.serviceTypeName }
            )
        }

        composable<CaregiverHome> {
            val caregiverId = sessionManager.getUserId()
            LaunchedEffect(caregiverId) {
                serviceRequestViewModel.loadCaregiverData(caregiverId)
            }
            CaregiverHomeScreen(
                onGoToServices = { navController.navigate(CaregiverService) },
                ownerRequests = caregiverApplicationDetails,
                onAcceptApplication = { applicationId ->
                    serviceRequestViewModel.acceptApplication(applicationId, caregiverId)
                },
                onRejectApplication = { applicationId ->
                    serviceRequestViewModel.rejectApplication(applicationId, caregiverId)
                },
                caregiverId = caregiverId
            )
        }

        composable<CaregiverFeed> {
            val caregiverId = sessionManager.getUserId()
            LaunchedEffect(caregiverId) {
                serviceRequestViewModel.loadAvailableRequests()
                serviceRequestViewModel.loadCaregiverData(caregiverId)
            }
            CaregiverFeedScreen(
                requests = availableRequests,
                onApplyToRequest = { requestId ->
                    serviceRequestViewModel.applyToRequest(requestId, caregiverId)
                    navController.navigate(CaregiverHome) { launchSingleTop = true }
                }
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
            val args = it.toRoute<CaregiverProfile>()
            val isOwnProfile = args.caregiverId == -1
            CaregiverProfileScreen(
                caregiverId = args.caregiverId,
                showLogout = isOwnProfile,
                onBack = { navController.popBackStack() },
                onLogout = {
                    if (isOwnProfile) {
                        sessionLogout(navController, userRoleViewModel)
                    }
                }
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