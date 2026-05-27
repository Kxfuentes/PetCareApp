package com.proyectopoo.petcareapp.navigation

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
import androidx.navigation.toRoute
import com.proyectopoo.petcareapp.LocalUserRoleViewModel
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.ui.screen.auth.LoginScreen
import com.proyectopoo.petcareapp.ui.screen.auth.PasswordRecoveryScreen
import com.proyectopoo.petcareapp.ui.screen.auth.RegisterScreen
import com.proyectopoo.petcareapp.ui.screen.auth.RoleSectionScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverFeedScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverHomeScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverServiceScreen
import com.proyectopoo.petcareapp.ui.screen.owner.CreateServiceScreen
import com.proyectopoo.petcareapp.ui.screen.owner.DogInfoScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerFeedScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerHomeScreen
import com.proyectopoo.petcareapp.ui.screen.owner.OwnerProfileScreen
import com.proyectopoo.petcareapp.ui.screen.caregiver.CaregiverProfileScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.data.repository.UserRepository
import com.proyectopoo.petcareapp.data.session.SessionManager
import com.proyectopoo.petcareapp.viewmodel.LoginViewModel
import com.proyectopoo.petcareapp.viewmodel.UserRoleViewModel


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
                    }
                }
            }

            LoginScreen(
                onLoginClick = { username, password, rememberSession ->
                    loginViewModel.login(
                        email = username,
                        password = password,
                        rememberSession = rememberSession
                    )
                },
                onGoToRegister = {
                    navController.navigate(Register)
                },
                onGoToPasswordRecovery = {
                    navController.navigate(PasswordRecovery)
                }
            )
        }

        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = { response, password ->
                    val userData = response.user ?: response.useer
                    if (userData != null) {
                        navController.navigate(
                            RoleSection(
                                userId = userData.id ?: 0,
                                username = userData.username ?: "",
                                email = userData.email ?: "",
                                password = password
                            )
                        ) {
                            popUpTo(Register) { inclusive = true }
                        }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
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
                    navController.navigate(DogInfo)
                },
                onCaregiverSelected = {
                    userRoleViewModel.setRole(UserRole.CAREGIVER)
                    navController.navigate(CaregiverHome) {
                        popUpTo(Login) { inclusive = true }
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
                onGoToFeed = { navController.navigate(OwnerFeed) },
                onGoToCreate = { navController.navigate(CreateService) },
                onEditPets = { navController.navigate(DogInfo) },
                onGoToOwnerProfile = { navController.navigate(OwnerProfile) }
            )
        }

        composable<OwnerFeed> {
            OwnerFeedScreen(
                onGoToOwnerProfile = { _ -> navController.navigate(OwnerProfile) }
            )
        }

        composable<CreateService> {

            CreateServiceScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<CaregiverHome> {
            CaregiverHomeScreen(
                onGoToFeed = { navController.navigate(CaregiverFeed) },
                onGoToCreate = { navController.navigate(CreateService) },
                onGoToServices = { navController.navigate(CaregiverService) },
                onGoToCaregiverProfile = { navController.navigate(CaregiverProfile) }
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
                onLogout = {
                    sessionLogout(navController, userRoleViewModel)
                }
            )
        }

        composable<CaregiverProfile> {
            CaregiverProfileScreen(
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

private fun sessionLogout(navController: NavHostController, userRoleViewModel: UserRoleViewModel) {
    val sessionManager = SessionManager(navController.context)
    sessionManager.clearSession()
    userRoleViewModel.clearRole()
    navController.navigate(Login) {
        popUpTo(0) { inclusive = true }
    }
}
