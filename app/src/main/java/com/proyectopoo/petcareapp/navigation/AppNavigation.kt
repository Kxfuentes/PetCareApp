package com.proyectopoo.petcareapp.navigation

/*
 * Comentario de modulo PetCare:
 * Navegacion principal. Define rutas y el paso de datos entre pantallas de la app.
 */

import com.proyectopoo.petcareapp.data.network.RoleUpdateRequest
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.proyectopoo.petcareapp.LocalUserRoleViewModel
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.data.local.entity.CaregiverEntity
import com.proyectopoo.petcareapp.data.local.entity.OwnerEntity
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.local.entity.UserEntity
import com.proyectopoo.petcareapp.data.local.entity.UserRoleType
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.data.network.RetrofitClient.apiService
import com.proyectopoo.petcareapp.data.repository.PetRepository
import com.proyectopoo.petcareapp.data.repository.OfferedServiceRepository
import com.proyectopoo.petcareapp.data.repository.ServiceApplicationRepository
import com.proyectopoo.petcareapp.data.repository.ServiceRequestRepository
import com.proyectopoo.petcareapp.data.repository.UserRepository
import com.proyectopoo.petcareapp.data.session.SessionManager
import com.proyectopoo.petcareapp.data.session.resolveStableUserId
import com.proyectopoo.petcareapp.data.session.upsertLocalUser
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.ui.screen.auth.LoginScreen
import com.proyectopoo.petcareapp.ui.screen.auth.PasswordRecoveryScreen
import com.proyectopoo.petcareapp.ui.screen.auth.RegisterScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverFeedScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverHomeScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverProfileScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverPublicProfileScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverServiceScreen
import com.proyectopoo.petcareapp.ui.screen.owner.RoleSectionScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.EditCaregiverProfileScreen
import com.proyectopoo.petcareapp.ui.screen.owner.CreateServiceScreen
import com.proyectopoo.petcareapp.ui.screen.owner.DogInfoScreen
import com.proyectopoo.petcareapp.ui.screen.owner.EditOwnerProfileScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerFeedScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerHomeScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerProfileScreen
import com.proyectopoo.petcareapp.ui.screen.owner.RequestOfferScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerPublicProfileScreen
import com.proyectopoo.petcareapp.viewmodel.CaregiverProfileViewModel
import com.proyectopoo.petcareapp.viewmodel.CaregiverServiceViewModel
import com.proyectopoo.petcareapp.viewmodel.DogViewModel
import com.proyectopoo.petcareapp.viewmodel.EditCaregiverProfileViewModel
import com.proyectopoo.petcareapp.viewmodel.EditOwnerProfileViewModel
import com.proyectopoo.petcareapp.viewmodel.LoginViewModel
import com.proyectopoo.petcareapp.viewmodel.OwnerProfileViewModel
import com.proyectopoo.petcareapp.viewmodel.ServiceRequestViewModel
import com.proyectopoo.petcareapp.viewmodel.UserRoleViewModel
import com.proyectopoo.petcareapp.notifications.AppNotifier
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    wsRefreshTick: Int = 0,
    sessionLogout: (NavHostController, UserRoleViewModel) -> Unit
) {
    val userRoleViewModel = LocalUserRoleViewModel.current
    val activeRole by userRoleViewModel.userRole.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val database = remember { PetCareDatabase.getDatabase(context) }
    val appNotifier = remember { AppNotifier(context, database.notificationDao()) }
    val offeredServiceRepository = remember {
        OfferedServiceRepository(database.offeredServiceDao(), apiService)
    }
    val scope = rememberCoroutineScope()

    val dogViewModel: DogViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                DogViewModel(PetRepository(database.petDao()))
            }
        }
    )

    val serviceRequestViewModel: ServiceRequestViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ServiceRequestViewModel(
                    requestRepo = ServiceRequestRepository(
                        database.serviceRequestDao(),
                        database.serviceRequestPetDao()
                    ),
                    applicationRepo = ServiceApplicationRepository(
                        database.serviceApplicationDao(),
                        database.serviceRequestDao(),
                        database.serviceBookingDao(),
                        apiService
                    ),
                    userDao = database.userDao(),
                    ownerDao = database.ownerDao(),
                    caregiverDao = database.caregiverDao(),
                    petDao = database.petDao(),
                    serviceTypeDao = database.serviceTypeDao(),
                    ratingDao = database.ratingDao(),
                    offeredServiceDao = database.offeredServiceDao(),
                    bookingDao = database.serviceBookingDao(),
                    notifier = appNotifier
                )
            }
        }
    )

    val performLogout: () -> Unit = {
        dogViewModel.clear()
        serviceRequestViewModel.clear()
        sessionLogout(navController, userRoleViewModel)
    }

    val dogs by dogViewModel.dogs.collectAsStateWithLifecycle()
    val recentOwnerRequests by serviceRequestViewModel.recentOwnerRequests.collectAsStateWithLifecycle()
    val ownerApplicationDetails by serviceRequestViewModel.ownerApplicationDetails.collectAsStateWithLifecycle()
    val caregiverApplicationDetails by serviceRequestViewModel.caregiverApplicationDetails.collectAsStateWithLifecycle()
    val availableRequests by serviceRequestViewModel.availableRequests.collectAsStateWithLifecycle()
    val currentUserId = sessionManager.getUserId()

    LaunchedEffect(currentUserId) {
        if (currentUserId > 0) {
            dogViewModel.loadDogs(currentUserId)
        }
    }

    LaunchedEffect(wsRefreshTick, currentUserId, activeRole) {
        if (currentUserId <= 0 || activeRole == null) return@LaunchedEffect

        when (activeRole) {
            UserRole.OWNER -> {
                dogViewModel.loadDogs(currentUserId)
                serviceRequestViewModel.loadOwnerData(currentUserId)
            }
            UserRole.CAREGIVER -> {
                serviceRequestViewModel.loadCaregiverData(currentUserId)
                serviceRequestViewModel.loadAvailableRequests(currentUserId)
            }
            null -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = Login,
        modifier = modifier
    ) {
        // ===== LOGIN =====
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
            var navigationError by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(loggedUser) {
                loggedUser?.let { user ->
                    val role = when (user.role.name) {
                        "CAREGIVER" -> UserRole.CAREGIVER
                        else -> UserRole.OWNER
                    }

                    try {
                        val stableUserId = prepareLocalAccount(
                            database = database,
                            sessionManager = sessionManager,
                            userId = user.userId,
                            username = user.fullName,
                            email = user.email,
                            role = user.role,
                            apiUserId = null
                        )

                        if (role == UserRole.OWNER) {
                            dogViewModel.loadDogs(stableUserId)
                            serviceRequestViewModel.loadOwnerData(stableUserId)
                        } else {
                            serviceRequestViewModel.loadCaregiverData(stableUserId)
                        }

                        navigationError = null
                        userRoleViewModel.setRole(role)

                        navController.navigate(
                            if (role == UserRole.CAREGIVER) CaregiverHome else OwnerHome
                        ) {
                            popUpTo(Login) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        navigationError = e.localizedMessage
                            ?: "No se pudo preparar la sesiÃ³n local."
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
                errorMessage = navigationError ?: errorMessage
            )
        }

        // ===== REGISTER =====
        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = { response ->
                    val userData = response.user ?: response.useer
                    if (userData != null && userData.id > 0 && userData.email.isNotBlank()) {
                        navController.navigate(
                            RoleSection(
                                userId = userData.id,
                                apiUserId = userData.id.toString(),
                                username = userData.username,
                                email = userData.email
                            )
                        ) {
                            popUpTo(Register) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ===== ROLE SECTION =====
        composable<RoleSection> { backStackEntry ->
            val data = backStackEntry.toRoute<RoleSection>()
            var isPreparingAccount by remember { mutableStateOf(false) }
            var onboardingError by remember { mutableStateOf<String?>(null) }

            RoleSectionScreen(
                userId = data.userId,
                username = data.username,
                email = data.email,
                onOwnerSelected = {
                    if (isPreparingAccount) return@RoleSectionScreen
                    scope.launch {
                        isPreparingAccount = true
                        onboardingError = null
                        try {
                            val roleResponse = RetrofitClient.apiService.updateUserRole(
                                userId = data.userId,
                                request = RoleUpdateRequest(rol = "propietario")
                            )
                            if (!roleResponse.isSuccessful) {
                                throw Exception("No se pudo guardar el rol de propietario. HTTP ${roleResponse.code()}")
                            }
                            prepareLocalAccount(
                                database = database,
                                sessionManager = sessionManager,
                                userId = data.userId,
                                username = data.username,
                                email = data.email,
                                role = UserRoleType.OWNER,
                                apiUserId = data.apiUserId
                            )
                            userRoleViewModel.setRole(UserRole.OWNER)
                            dogViewModel.loadDogs(sessionManager.getUserId())
                            navController.navigate(DogInfo()) {
                                popUpTo(Login) { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            onboardingError = e.localizedMessage ?: "No se pudo preparar tu cuenta. Intenta de nuevo."
                        } finally {
                            isPreparingAccount = false
                        }
                    }
                },
                onCaregiverSelected = {
                    if (isPreparingAccount) return@RoleSectionScreen
                    scope.launch {
                        isPreparingAccount = true
                        onboardingError = null
                        try {
                            val roleResponse = RetrofitClient.apiService.updateUserRole(
                                userId = data.userId,
                                request = RoleUpdateRequest(rol = "gestor")
                            )
                            if (!roleResponse.isSuccessful) {
                                throw Exception("No se pudo guardar el rol de cuidador. HTTP ${roleResponse.code()}")
                            }
                            prepareLocalAccount(
                                database = database,
                                sessionManager = sessionManager,
                                userId = data.userId,
                                username = data.username,
                                email = data.email,
                                role = UserRoleType.CAREGIVER,
                                apiUserId = data.apiUserId
                            )
                            userRoleViewModel.setRole(UserRole.CAREGIVER)
                            serviceRequestViewModel.loadCaregiverData(sessionManager.getUserId())
                            navController.navigate(CaregiverHome) {
                                popUpTo(Login) { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            onboardingError = e.localizedMessage ?: "No se pudo preparar tu cuenta. Intenta de nuevo."
                        } finally {
                            isPreparingAccount = false
                        }
                    }
                },
                isLoading = isPreparingAccount,
                errorMessage = onboardingError
            )
        }

        // ===== DOG INFO =====
        composable<DogInfo> { backStackEntry ->
            val args = backStackEntry.toRoute<DogInfo>()
            val ownerId = sessionManager.getUserId()
            LaunchedEffect(ownerId, args.petId) {
                if (ownerId > 0) {
                    dogViewModel.loadDogs(ownerId)
                }
            }
            val editingDog = dogs.find { it.petId == args.petId }
            if (ownerId > 0) {
                DogInfoScreen(
                    editingDog = editingDog,
                    onFinish = { name, breed, size ->
                        val isNewDog = args.petId == -1
                        val petId = if (isNewDog) generatePetId(dogs) else args.petId
                        val pet = PetEntity(
                            petId = petId,
                            ownerId = ownerId,
                            name = name,
                            breed = breed,
                            size = size,
                            species = "Dog"
                        )
                        scope.launch {
                            try {
                                ensureOwnerExists(database, sessionManager, ownerId)
                                if (isNewDog) {
                                    dogViewModel.addDog(pet)
                                } else {
                                    dogViewModel.updateDog(pet)
                                }
                                Toast.makeText(context, "Mascota guardada correctamente", Toast.LENGTH_SHORT).show()
                                navController.navigate(OwnerHome) {
                                    popUpTo(DogInfo()) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, e.localizedMessage ?: "No se pudo guardar la mascota.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }

        // ===== OWNER HOME =====
        composable<OwnerHome> {
            val ownerId = sessionManager.getUserId()
            LaunchedEffect(ownerId) {
                if (ownerId > 0) {
                    dogViewModel.loadDogs(ownerId)
                    serviceRequestViewModel.loadOwnerData(ownerId)
                }
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
                onGoToOwnerProfile = {
                    navController.navigate(OwnerProfile(ownerId = ownerId))
                },
                onAcceptApplication = { applicationId ->
                    serviceRequestViewModel.acceptApplication(applicationId, ownerId = ownerId)
                },
                onRejectApplication = { applicationId ->
                    serviceRequestViewModel.rejectApplication(applicationId, ownerId)
                },
                onCompleteAndRate = { application, score, comment ->
                    serviceRequestViewModel.completeAndRateService(
                        applicationId = application.applicationId,
                        serviceRequestId = application.serviceRequestId,
                        caregiverId = application.caregiverId,
                        ownerId = application.ownerId,
                        ratedByRole = UserRoleType.OWNER,
                        score = score,
                        comment = comment,
                        reloadOwnerId = ownerId
                    )
                },
                ownerId = ownerId
            )
        }

        // ===== OWNER FEED =====
        composable<OwnerFeed> {
            val ownerId = sessionManager.getUserId()
            var offeredServices by remember { mutableStateOf(emptyList<com.proyectopoo.petcareapp.data.local.relation.OfferedServiceDetails>()) }
            LaunchedEffect(ownerId) {
                serviceRequestViewModel.loadOwnerData(ownerId)
                offeredServices = offeredServiceRepository.getAvailableServiceDetailsFromApi()
            }
            OwnerFeedScreen(
                services = offeredServices,
                onGoToCaregiverProfile = { caregiverId ->
                    navController.navigate(CaregiverProfile(caregiverId = caregiverId))
                },
                onRequestService = { caregiverId, offeredServiceId ->
                    navController.navigate(
                        RequestOffer(
                            offeredServiceId = offeredServiceId,
                            caregiverId = caregiverId
                        )
                    )
                }
            )
        }

        // ===== REQUEST OFFER =====
        composable<RequestOffer> { backStackEntry ->
            val args = backStackEntry.toRoute<RequestOffer>()
            val ownerId = sessionManager.getUserId()
            var offerDetails by remember { mutableStateOf<com.proyectopoo.petcareapp.data.local.relation.OfferedServiceDetails?>(null) }
            LaunchedEffect(args.offeredServiceId) {
                dogViewModel.loadDogs(ownerId)
                val allOffers = offeredServiceRepository.getAvailableServiceDetailsFromApi()
                offerDetails = allOffers.find { it.offeredServiceId == args.offeredServiceId }
            }
            offerDetails?.let { offer ->
                RequestOfferScreen(
                    offer = offer,
                    dogs = dogs,
                    onBack = { navController.popBackStack() },
                    onSubmit = { petIds, date, startTime, notes ->
                        serviceRequestViewModel.requestServiceFromOffer(
                            ownerId = ownerId,
                            caregiverId = args.caregiverId,
                            offeredServiceId = args.offeredServiceId,
                            petIds = petIds,
                            requestedDate = date,
                            startTime = startTime,
                            notes = notes,
                            suggestedPrice = null
                        )
                        Toast.makeText(context, "Solicitud de reserva enviada.", Toast.LENGTH_SHORT).show()
                        navController.navigate(OwnerHome) { launchSingleTop = true }
                    }
                )
            }
        }

        // ===== CREATE SERVICE =====
        composable<CreateService> { backStackEntry ->
            val args = backStackEntry.toRoute<CreateService>()
            val ownerId = sessionManager.getUserId()
            CreateServiceScreen(
                petName = args.petName,
                serviceType = args.serviceType,
                dogs = dogs,
                onBack = { navController.popBackStack() },
                onPublish = { selectedPetNames, serviceType, description, location, price, date, startTime, endTime, lat, lon ->
                    val petIds = dogs
                        .filter { dog -> selectedPetNames.any { it.equals(dog.name, ignoreCase = true) } }
                        .map { it.petId }
                    if (petIds.isEmpty()) return@CreateServiceScreen
                    serviceRequestViewModel.createRequestFromForm(
                        ownerId = ownerId,
                        petIds = petIds,
                        serviceTypeName = serviceType,
                        description = description,
                        location = location,
                        price = price,
                        requestedDate = date,
                        startTime = startTime,
                        endTime = endTime,
                        latitude = lat,
                        longitude = lon
                    )
                    navController.navigate(OwnerHome) { launchSingleTop = true }
                }
            )
        }

        // ===== CAREGIVER HOME =====
        composable<CaregiverHome> {
            val caregiverId = sessionManager.getUserId()
            LaunchedEffect(caregiverId) {
                if (caregiverId > 0) {
                    serviceRequestViewModel.loadCaregiverData(caregiverId)
                }
            }
            CaregiverHomeScreen(
                onGoToServices = { navController.navigate(CaregiverService) },
                ownerRequests = caregiverApplicationDetails,
                onAcceptApplication = { applicationId ->
                    serviceRequestViewModel.acceptApplication(applicationId, caregiverId = caregiverId)
                },
                onRejectApplication = { applicationId ->
                    serviceRequestViewModel.rejectApplication(applicationId, caregiverId)
                },
                onCompleteAndRate = { request, score, comment ->
                    serviceRequestViewModel.markDoneByCaregiverAndRateOwner(
                        applicationId = request.applicationId,
                        serviceRequestId = request.serviceRequestId,
                        caregiverId = request.caregiverId,
                        ownerId = request.ownerId,
                        score = score,
                        comment = comment,
                        reloadCaregiverId = caregiverId
                    )
                },
                onCancelService = { request ->
                    serviceRequestViewModel.cancelService(
                        applicationId = request.applicationId,
                        reloadCaregiverId = caregiverId
                    )
                },
                caregiverId = caregiverId
            )
        }

        // ===== CAREGIVER FEED =====
        composable<CaregiverFeed> {
            val caregiverId = sessionManager.getUserId()
            LaunchedEffect(caregiverId) {
                if (caregiverId > 0) {
                    serviceRequestViewModel.loadAvailableRequests(caregiverId)
                }
                if (caregiverId > 0) {
                    serviceRequestViewModel.loadCaregiverData(caregiverId)
                }
            }
            CaregiverFeedScreen(
                requests = availableRequests,
                onGoToOwnerProfile = { ownerId, serviceRequestId ->
                    navController.navigate(
                        OwnerProfile(
                            ownerId = ownerId,
                            serviceRequestId = serviceRequestId
                        )
                    )
                },
                onApplyToRequest = { serviceRequestId ->
                    serviceRequestViewModel.applyToRequest(serviceRequestId, caregiverId)
                    Toast.makeText(context, "Solicitud de trabajo enviada.", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ===== CAREGIVER SERVICE =====
        composable<CaregiverService> {
            val caregiverId = sessionManager.getUserId()
            val caregiverServiceViewModel: CaregiverServiceViewModel = viewModel(
                key = "caregiver_services_$caregiverId",
                factory = viewModelFactory {
                    initializer {
                        CaregiverServiceViewModel(
                            offeredServiceRepository = offeredServiceRepository,
                            serviceTypeDao = database.serviceTypeDao(),
                            caregiverId = caregiverId
                        )
                    }
                }
            )
            CaregiverServiceScreen(
                onBack = { navController.popBackStack() },
                caregiverId = caregiverId,
                viewModel = caregiverServiceViewModel
            )
        }

        // ===== OWNER PROFILE =====
        composable<OwnerProfile> { backStackEntry ->
            val args = backStackEntry.toRoute<OwnerProfile>()
            val loggedUserId = sessionManager.getUserId()
            val isOwnProfile = args.ownerId <= 0 || args.ownerId == loggedUserId
            val targetOwnerId = if (isOwnProfile) loggedUserId else args.ownerId
            val profileViewModel: OwnerProfileViewModel = viewModel(
                key = "owner_profile_$targetOwnerId",
                factory = viewModelFactory {
                    initializer {
                        OwnerProfileViewModel(
                            userDao = database.userDao(),
                            petDao = database.petDao(),
                            ownerId = targetOwnerId
                        )
                    }
                }
            )
            val user by profileViewModel.user.collectAsStateWithLifecycle()
            if (isOwnProfile) {
                LaunchedEffect(targetOwnerId) {
                    serviceRequestViewModel.loadOwnerData(targetOwnerId)
                }
                OwnerProfileScreen(
                    user = user,
                    dogs = dogs,
                    historyServices = recentOwnerRequests.filter { it.status.name == "COMPLETED" },
                    onLogout = performLogout,
                    onEditProfile = {
                        navController.navigate(EditOwnerProfile(targetOwnerId))
                    },
                    onAddPet = { navController.navigate(DogInfo()) }
                )
            } else {
                OwnerPublicProfileScreen(
                    ownerId = targetOwnerId,
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() },
                    onRequestServices = {
                        if (args.serviceRequestId == -1) {
                            Toast.makeText(context, "Abre el perfil desde una solicitud disponible.", Toast.LENGTH_SHORT).show()
                        } else {
                            serviceRequestViewModel.applyToRequest(args.serviceRequestId, loggedUserId)
                            Toast.makeText(context, "Solicitud de trabajo enviada.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        // ===== CAREGIVER PROFILE =====
        composable<CaregiverProfile> { backStackEntry ->
            val args = backStackEntry.toRoute<CaregiverProfile>()
            val loggedUserId = sessionManager.getUserId()
            val isOwnProfile = args.caregiverId <= 0 || args.caregiverId == loggedUserId
            val targetCaregiverId = if (isOwnProfile) loggedUserId else args.caregiverId
            val viewModel: CaregiverProfileViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        CaregiverProfileViewModel(
                            database = database,
                            caregiverId = targetCaregiverId
                        )
                    }
                }
            )
            val user by viewModel.user.collectAsStateWithLifecycle()
            val completedServicesCount by viewModel.completedServicesCount.collectAsStateWithLifecycle()
            val rating by viewModel.rating.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

            if (isOwnProfile) {
                CaregiverProfileScreen(
                    user = user,
                    caregiverId = targetCaregiverId,
                    isOwnProfile = true,
                    completedServicesCount = completedServicesCount,
                    rating = rating,
                    isLoading = isLoading,
                    onBack = { navController.popBackStack() },
                    onLogout = performLogout,
                    onEditProfile = {
                        navController.navigate(EditCaregiverProfile(targetCaregiverId))
                    },
                    onManageAvailability = { }
                )
            } else {
                CaregiverPublicProfileScreen(
                    caregiverId = targetCaregiverId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRequestServices = {
                        scope.launch {
                            val offers = offeredServiceRepository.getAvailableServicesByCaregiver(targetCaregiverId)
                            val firstOffer = offers.firstOrNull()
                            if (firstOffer == null) {
                                Toast.makeText(context, "Este cuidador no tiene ofertas activas disponibles.", Toast.LENGTH_SHORT).show()
                            } else {
                                navController.navigate(
                                    RequestOffer(
                                        offeredServiceId = firstOffer.offeredServiceId,
                                        caregiverId = targetCaregiverId
                                    )
                                )
                            }
                        }
                    }
                )
            }
        }

        // ===== EDIT OWNER PROFILE =====
        composable<EditOwnerProfile> { backStackEntry ->
            val args = backStackEntry.toRoute<EditOwnerProfile>()
            val ownerId = if (args.ownerId == -1) sessionManager.getUserId() else args.ownerId
            val viewModel: EditOwnerProfileViewModel = viewModel(
                key = "edit_owner_profile_$ownerId",
                factory = viewModelFactory {
                    initializer {
                        EditOwnerProfileViewModel(
                            userDao = database.userDao(),
                            ownerId = ownerId
                        )
                    }
                }
            )
            EditOwnerProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // ===== EDIT CAREGIVER PROFILE =====
        composable<EditCaregiverProfile> { backStackEntry ->
            val args = backStackEntry.toRoute<EditCaregiverProfile>()
            val caregiverId = if (args.caregiverId == -1) sessionManager.getUserId() else args.caregiverId
            val viewModel: EditCaregiverProfileViewModel = viewModel(
                key = "edit_caregiver_profile_$caregiverId",
                factory = viewModelFactory {
                    initializer {
                        EditCaregiverProfileViewModel(
                            database = database,
                            caregiverId = caregiverId
                        )
                    }
                }
            )
            EditCaregiverProfileScreen(
                caregiverId = caregiverId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // ===== PASSWORD RECOVERY =====
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

// ========== FUNCIONES AUXILIARES ==========

private fun generatePetId(existingPets: List<PetEntity>): Int {
    var candidate = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    if (candidate <= 0) candidate = 1
    val usedIds = existingPets.map { it.petId }.toSet()
    while (candidate in usedIds) {
        candidate = if (candidate == Int.MAX_VALUE) 1 else candidate + 1
    }
    return candidate
}

private suspend fun prepareLocalAccount(
    database: PetCareDatabase,
    sessionManager: SessionManager,
    userId: Int,
    username: String,
    email: String,
    role: UserRoleType,
    apiUserId: String? = null
): Int {
    require(email.isNotBlank()) { "La API devolviÃ³ un correo invÃ¡lido." }
    val stableUserId = resolveStableUserId(
        userDao = database.userDao(),
        email = email,
        apiUserId = apiUserId ?: userId.toString()
    )
    upsertLocalUser(
        userDao = database.userDao(),
        userId = stableUserId,
        fullName = username,
        email = email,
        role = role
    )
    when (role) {
        UserRoleType.OWNER -> {
            if (database.ownerDao().getOwnerById(stableUserId) == null) {
                database.ownerDao().insertOwner(OwnerEntity(ownerId = stableUserId, userId = stableUserId))
            }
        }
        UserRoleType.CAREGIVER -> {
            if (database.caregiverDao().getCaregiverById(stableUserId) == null) {
                database.caregiverDao().insertCaregiver(CaregiverEntity(caregiverId = stableUserId, userId = stableUserId))
            }
        }
    }
    sessionManager.saveSession(
        userId = stableUserId,
        email = email,
        role = role,
        apiUserId = apiUserId
    )
    return stableUserId
}

private suspend fun ensureOwnerExists(
    database: PetCareDatabase,
    sessionManager: SessionManager,
    ownerId: Int
) {
    if (database.ownerDao().getOwnerById(ownerId) != null) return
    val email = sessionManager.getEmail().orEmpty()
    if (database.userDao().getUserById(ownerId) == null) {
        database.userDao().insertUser(
            UserEntity(
                userId = ownerId,
                fullName = email.substringBefore("@").ifBlank { "DueÃ±o" },
                email = email.ifBlank { "dueno$ownerId@petcare.local" },
                password = null,
                role = UserRoleType.OWNER
            )
        )
    }
    database.ownerDao().insertOwner(OwnerEntity(ownerId = ownerId, userId = ownerId))
}
