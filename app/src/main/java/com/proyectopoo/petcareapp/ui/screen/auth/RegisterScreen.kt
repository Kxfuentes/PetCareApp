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
import com.proyectopoo.petcareapp.data.network.SendOtpRequest
import com.proyectopoo.petcareapp.data.network.VerifyOtpRequest
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

    // Paso de verificacion de identidad: se pide el OTP antes de crear la cuenta.
    var otpStep by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var devOtpHint by remember { mutableStateOf<String?>(null) }

    suspend fun performRegister() {
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

                if (registerResponse != null && registeredUser != null && registeredUser.id > 0) {
                    onRegisterSuccess(registerResponse)
                } else {
                    errorMessage = "La API respondió OK, pero no devolvió usuario. Body: ${response.body()}"
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val normalizedError = errorBody.orEmpty().lowercase()
                errorMessage = when {
                    normalizedError.contains("duplicate") ||
                        normalizedError.contains("duplicada") ||
                        normalizedError.contains("duplicado") ||
                        normalizedError.contains("already") ||
                        normalizedError.contains("ya está registrado") ||
                        normalizedError.contains("unique") -> "Este correo electrónico o usuario ya está registrado"
                    normalizedError.contains("invalid email") ||
                        normalizedError.contains("email inválido") ||
                        normalizedError.contains("email invalido") ||
                        normalizedError.contains("format") -> "Error con el formato del correo"
                    else -> "HTTP ${response.code()}: $errorBody"
                }
            }
        } catch (e: SocketTimeoutException) {
            errorMessage = "No se pudo conectar con la API. Revisa que el servidor esté activo y que BASE_URL apunte a tu computadora."
        } catch (e: Exception) {
            errorMessage = "Error de conexión: ${e.localizedMessage ?: "no se pudo contactar la API"}"
        }
    }

    fun requestOtp() {
        errorMessage = null
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.apiService.sendOtp(SendOtpRequest(email))
                if (response.isSuccessful) {
                    devOtpHint = response.body()?.otp
                    otpStep = true
                } else {
                    errorMessage = "No se pudo enviar el código de verificación. Intenta de nuevo."
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.localizedMessage ?: "no se pudo contactar la API"}"
            } finally {
                isLoading = false
            }
        }
    }

    fun validate(): String? {
        if (username.isBlank()) return "El nombre de usuario es requerido"
        if (username.length < 3) return "Debe tener al menos 3 caracteres"
        if (!username.matches("^[a-zA-Z0-9]+$".toRegex()))
            return "Solo letras y números permitidos"

        if (email.isBlank()) return "El correo es requerido"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "Formato de correo inválido"

        if (password.isBlank()) return "La contraseña es requerida"
        if (password.length < 3) return "Mínimo 3 caracteres"
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
                enabled = !isLoading && !otpStep
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
                enabled = !isLoading && !otpStep
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
                enabled = !isLoading && !otpStep
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
                enabled = !isLoading && !otpStep
            )

            if (otpStep) {
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    "Enviamos un código de verificación a $email.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                devOtpHint?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Modo de prueba (sin correo real configurado): tu código es $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                    label = { Text("Código de verificación") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { requestOtp() }, enabled = !isLoading) {
                    Text("Reenviar código")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!otpStep) {
                        val validationError = validate()
                        if (validationError != null) {
                            errorMessage = validationError
                            return@Button
                        }
                        requestOtp()
                        return@Button
                    }

                    if (otpCode.length != 6) {
                        errorMessage = "Ingresa el código de 6 dígitos"
                        return@Button
                    }

                    errorMessage = null
                    isLoading = true

                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.verifyOtp(VerifyOtpRequest(email, otpCode))
                            if (response.isSuccessful && response.body()?.verified == true) {
                                performRegister()
                            } else {
                                errorMessage = "Código incorrecto o expirado."
                            }
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
                    Text(
                        if (otpStep) "Verificar y crear cuenta" else "Crear cuenta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
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
