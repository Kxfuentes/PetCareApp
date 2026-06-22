package com.proyectopoo.petcareapp.navigation

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
import com.proyectopoo.petcareapp.data.repository.PetRepository
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
import com.proyectopoo.petcareapp.ui.screen.caregiver.EditCaregiverProfileScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverPublicProfileScreen
import com.proyectopoo.petcareapp.ui.screen.owner.CreateServiceScreen
import com.proyectopoo.petcareapp.ui.screen.owner.DogInfoScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerFeedScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerHomeScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerProfileScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerPublicProfileScreen
import com.proyectopoo.petcareapp.ui.screen.owner.EditOwnerProfileScreen
import com.proyectopoo.petcareapp.viewmodel.CaregiverProfileViewModel
import com.proyectopoo.petcareapp.viewmodel.CaregiverServiceViewModel
import com.proyectopoo.petcareapp.viewmodel.DogViewModel
import com.proyectopoo.petcareapp.viewmodel.LoginViewModel
import com.proyectopoo.petcareapp.viewmodel.OwnerProfileViewModel
import com.proyectopoo.petcareapp.viewmodel.ServiceRequestViewModel
import com.proyectopoo.petcareapp.viewmodel.UserRoleViewModel
import com.proyectopoo.petcareapp.viewmodel.EditOwnerProfileViewModel
import com.proyectopoo.petcareapp.viewmodel.EditCaregiverProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sessionLogout: (NavHostController, UserRoleViewModel) -> Unit
) {
    val userRoleViewModel = LocalUserRoleViewModel.current
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val database = PetCareDatabase.getDatabase(context)
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
    val currentUserId = sessionManager.getUserId()

    LaunchedEffect(currentUserId) {
        if (currentUserId > 0) {
            dogViewModel.loadDogs(currentUserId)
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
                        prepareLocalAccount(
                            database = database,
                            sessionManager = sessionManager,
                            userId = user.userId,
                            username = user.fullName,
                            email = user.email,
                            role = user.role
                        )
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
                            ?: "No se pudo preparar la sesión local."
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
                            prepareLocalAccount(
                                database = database,
                                sessionManager = sessionManager,
                                userId = data.userId,
                                username = data.username,
                                email = data.email,
                                role = UserRoleType.OWNER
                            )
                            userRoleViewModel.setRole(UserRole.OWNER)
                            dogViewModel.loadDogs(data.userId)
                            navController.navigate(DogInfo()) {
                                popUpTo(Login) { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            onboardingError = e.localizedMessage
                                ?: "No se pudo preparar tu cuenta. Intenta de nuevo."
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
                            prepareLocalAccount(
                                database = database,
                                sessionManager = sessionManager,
                                userId = data.userId,
                                username = data.username,
                                email = data.email,
                                role = UserRoleType.CAREGIVER
                            )
                            userRoleViewModel.setRole(UserRole.CAREGIVER)
                            navController.navigate(CaregiverHome) {
                                popUpTo(Login) { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            onboardingError = e.localizedMessage
                                ?: "No se pudo preparar tu cuenta. Intenta de nuevo."
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
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    e.localizedMessage ?: "No se pudo guardar la mascota.",
                                    Toast.LENGTH_LONG
                                ).show()
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
                onGoToOwnerProfile = {
                    navController.navigate(OwnerProfile(ownerId = ownerId))
                },
                onAcceptApplication = { applicationId ->
                    serviceRequestViewModel.acceptApplication(applicationId, ownerId)
                },
                onRejectApplication = { applicationId ->
                    serviceRequestViewModel.rejectApplication(applicationId, ownerId)
                },
                ownerId = ownerId
            )
        }

        // ===== OWNER FEED =====
        composable<OwnerFeed> {
            val ownerId = sessionManager.getUserId()
            LaunchedEffect(ownerId) {
                serviceRequestViewModel.loadOwnerData(ownerId)
            }
            OwnerFeedScreen(
                onGoToCaregiverProfile = { caregiverId ->
                    navController.navigate(CaregiverProfile(caregiverId = caregiverId))
                },
                onRequestServices = { caregiverId ->
                    val request = recentOwnerRequests.firstOrNull()
                    if (request == null) {
                        Toast.makeText(
                            context,
                            "Primero crea una solicitud de servicio.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        serviceRequestViewModel.applyToRequest(request.serviceRequestId, caregiverId)
                        Toast.makeText(
                            context,
                            "Solicitud enviada al cuidador.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        // ===== CREATE SERVICE (ACTUALIZADO CON startTime Y endTime) =====
        composable<CreateService> { backStackEntry ->
            val args = backStackEntry.toRoute<CreateService>()
            val ownerId = sessionManager.getUserId()
            CreateServiceScreen(
                petName = args.petName,
                serviceType = args.serviceType,
                dogs = dogs,
                onBack = { navController.popBackStack() },
                onPublish = { petName, serviceType, description, location, price, date, startTime, endTime ->
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
                        requestedDate = date,
                        startTime = startTime,
                        endTime = endTime
                    )
                    navController.navigate(OwnerHome) { launchSingleTop = true }
                },
                existingServices = recentOwnerRequests.mapNotNull { it.serviceTypeName }
            )
        }

        // ===== CAREGIVER HOME =====
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

        // ===== CAREGIVER FEED =====
        composable<CaregiverFeed> {
            val caregiverId = sessionManager.getUserId()
            LaunchedEffect(caregiverId) {
                serviceRequestViewModel.loadAvailableRequests()
                serviceRequestViewModel.loadCaregiverData(caregiverId)
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
                    Toast.makeText(
                        context,
                        "Solicitud de trabajo enviada.",
                        Toast.LENGTH_SHORT
                    ).show()
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
                            offeredServiceDao = database.offeredServiceDao(),
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

            val isOwnProfile = args.ownerId == -1 || args.ownerId == loggedUserId
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
            val dogs by profileViewModel.dogs.collectAsStateWithLifecycle()

            if (isOwnProfile) {
                OwnerProfileScreen(
                    user = user,
                    dogs = dogs,
                    historyServices = recentOwnerRequests.filter { it.status.name == "COMPLETED" },
                    onLogout = { sessionLogout(navController, userRoleViewModel) },
                    onEditProfile = { navController.navigate(EditOwnerProfile(targetOwnerId)) },
                    onAddPet = { navController.navigate(DogInfo()) }
                )
            } else {
                OwnerPublicProfileScreen(
                    ownerId = targetOwnerId,
                    viewModel = profileViewModel,
                    onBack = { navController.popBackStack() },
                    onRequestServices = {
                        if (args.serviceRequestId == -1) {
                            Toast.makeText(
                                context,
                                "Abre el perfil desde una solicitud disponible.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            serviceRequestViewModel.applyToRequest(args.serviceRequestId, loggedUserId)
                            Toast.makeText(
                                context,
                                "Solicitud de trabajo enviada.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }

        // ===== CAREGIVER PROFILE =====
        composable<CaregiverProfile> { backStackEntry ->
            val args = backStackEntry.toRoute<CaregiverProfile>()
            val loggedUserId = sessionManager.getUserId()

            val isOwnProfile = args.caregiverId == -1 || args.caregiverId == loggedUserId
            val targetCaregiverId = if (isOwnProfile) loggedUserId else args.caregiverId

            val caregiverProfileViewModel: CaregiverProfileViewModel = viewModel(
                key = "caregiver_profile_$targetCaregiverId",
                factory = viewModelFactory {
                    initializer {
                        CaregiverProfileViewModel(
                            database = database,
                            caregiverId = targetCaregiverId
                        )
                    }
                }
            )

            val user by caregiverProfileViewModel.user.collectAsStateWithLifecycle()
            val completedServicesCount by caregiverProfileViewModel.completedServicesCount.collectAsStateWithLifecycle()
            val rating by caregiverProfileViewModel.rating.collectAsStateWithLifecycle()
            val isLoading by caregiverProfileViewModel.isLoading.collectAsStateWithLifecycle()

            if (isOwnProfile) {
                CaregiverProfileScreen(
                    user = user,
                    caregiverId = targetCaregiverId,
                    isOwnProfile = true,
                    completedServicesCount = completedServicesCount,
                    rating = rating,
                    isLoading = isLoading,
                    onBack = { navController.popBackStack() },
                    onLogout = { sessionLogout(navController, userRoleViewModel) },
                    onEditProfile = { navController.navigate(EditCaregiverProfile(targetCaregiverId)) },
                    onManageAvailability = { /* implementar */ }
                )
            } else {
                CaregiverPublicProfileScreen(
                    caregiverId = targetCaregiverId,
                    viewModel = caregiverProfileViewModel,
                    onBack = { navController.popBackStack() },
                    onRequestServices = {
                        val request = recentOwnerRequests.firstOrNull()
                        if (request == null) {
                            Toast.makeText(
                                context,
                                "Primero crea una solicitud de servicio.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            serviceRequestViewModel.applyToRequest(request.serviceRequestId, targetCaregiverId)
                            Toast.makeText(
                                context,
                                "Solicitud enviada al cuidador.",
                                Toast.LENGTH_SHORT
                            ).show()
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

    val usedIds = existingPets.map { it.petId }.toHashSet()
    while (candidate in usedIds) {
        candidate = if (candidate == Int.MAX_VALUE) 1 else candidate + 1
    }

    return candidate
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
                fullName = email.substringBefore("@").ifBlank { "Dueño" },
                email = email.ifBlank { "dueno$ownerId@petcare.local" },
                password = null,
                role = UserRoleType.OWNER
            )
        )
    }

    database.ownerDao().insertOwner(
        OwnerEntity(
            ownerId = ownerId,
            userId = ownerId
        )
    )
}

private suspend fun prepareLocalAccount(
    database: PetCareDatabase,
    sessionManager: SessionManager,
    userId: Int,
    username: String,
    email: String,
    role: UserRoleType
) {
    require(userId > 0) { "La API devolvió un ID de usuario inválido." }
    require(email.isNotBlank()) { "La API devolvió un correo inválido." }

    database.userDao().insertUser(
        UserEntity(
            userId = userId,
            fullName = username.ifBlank { email.substringBefore("@") },
            email = email,
            password = null,
            role = role
        )
    )

    when (role) {
        UserRoleType.OWNER -> database.ownerDao().insertOwner(
            OwnerEntity(
                ownerId = userId,
                userId = userId
            )
        )

        UserRoleType.CAREGIVER -> database.caregiverDao().insertCaregiver(
            CaregiverEntity(
                caregiverId = userId,
                userId = userId
            )
        )
    }

    sessionManager.saveSession(
        userId = userId,
        email = email,
        role = role
    )
}
