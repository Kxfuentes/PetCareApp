package com.proyectopoo.petcareapp.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.proyectopoo.petcareapp.ui.services.CreateServiceScreen
import com.proyectopoo.petcareapp.ui.services.FeedScreen
import kotlinx.coroutines.launch

enum class Screen {
    LOGIN,
    REGISTER,
    ROLES,
    FEED,
    CREATE_SERVICE
}

@Composable
fun PetCareApp() {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                Screen.LOGIN -> LoginScreen(
                    username = username,
                    onUsernameChange = { username = it },
                    password = password,
                    onPasswordChange = { password = it },
                    onLoginClick = {
                        if (username.isNotBlank() && password.isNotBlank()) {
                            currentScreen = Screen.ROLES
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Completa todos los campos")
                            }
                        }
                    },
                    onRegisterClick = { currentScreen = Screen.REGISTER }
                )

                Screen.REGISTER -> RegisterScreen(
                    username = username,
                    onUsernameChange = { username = it },
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    onRegisterClick = {
                        if (
                            username.isNotBlank() &&
                            email.isNotBlank() &&
                            password.isNotBlank()
                        ) {
                            currentScreen = Screen.ROLES
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Completa todos los campos")
                            }
                        }
                    },
                    onLoginClick = { currentScreen = Screen.LOGIN }
                )

                Screen.ROLES -> RolesScreen(
                    onRoleSelected = { role ->
                        currentScreen = if (role == "Cuidador") {
                            Screen.FEED
                        } else {
                            Screen.CREATE_SERVICE
                        }
                    }
                )

                Screen.FEED -> FeedScreen()

                Screen.CREATE_SERVICE -> CreateServiceScreen()
            }
        }
    }
}