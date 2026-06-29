package com.proyectopoo.petcareapp.ui.screen.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.data.network.RegisterRequest
import com.proyectopoo.petcareapp.data.network.RegisterResponse
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (RegisterResponse) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        if (username.isBlank()) return "El nombre de usuario es requerido"
        if (username.length < 3) return "Debe tener al menos 3 caracteres"
        if (!username.matches("^[a-zA-Z0-9]+$".toRegex()))
            return "Solo letras y números permitidos"

        if (email.isBlank()) return "El correo es requerido"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "Formato de correo inválido"

        if (password.isBlank()) return "La contraseña es requerida"
        if (password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        if (!password.any { !it.isLetterOrDigit() })
            return "La contraseña debe incluir un carácter especial"

        if (password != confirmPassword) return "Las contraseñas no coinciden"

        return null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Únete a PetCare",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Crea tu cuenta en segundos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                leadingIcon = { Icon(Icons.Outlined.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            null
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val validationError = validate()
                    if (validationError != null) {
                        errorMessage = validationError
                        return@Button
                    }

                    errorMessage = null
                    isLoading = true

                    scope.launch {
                        try {
                            val request = RegisterRequest(
                                username = username,
                                email = email,
                                password = password,
                                rol = null
                            )

                            val response = RetrofitClient.apiService.registerUser(request)

                            if (response.isSuccessful) {
                                val registerResponse = response.body()
                                val registeredUser = registerResponse?.user ?: registerResponse?.useer

                                // 🔥 CORREGIDO: id es Int, usar > 0 en lugar de isNotBlank()
                                if (registerResponse != null && registeredUser != null && registeredUser.id > 0 && registeredUser.email.isNotBlank()) {
                                    onRegisterSuccess(registerResponse)
                                } else {
                                    errorMessage = "La API respondió OK, pero no devolvió usuario. Body: ${response.body()}"
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                errorMessage = when {
                                    errorBody?.contains("duplicada") == true -> "Este correo electrónico ya está registrado"
                                    errorBody?.contains("email") == true -> "Error con el formato del correo"
                                    else -> "HTTP ${response.code()}: $errorBody"
                                }
                            }
                        } catch (e: SocketTimeoutException) {
                            errorMessage = "No se pudo conectar con la API. Revisa que el servidor esté activo y que BASE_URL apunte a tu computadora."
                        } catch (e: Exception) {
                            errorMessage = "Error de conexión: ${e.localizedMessage ?: "no se pudo contactar la API"}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear cuenta", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Volver")
            }
        }
    }
}