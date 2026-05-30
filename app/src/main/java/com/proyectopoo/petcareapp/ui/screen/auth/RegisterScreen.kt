package com.proyectopoo.petcareapp.ui.screen.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.data.network.*
import com.proyectopoo.petcareapp.ui.components.AuthTextField
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (RegisterResponse, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    fun validate(): String? {
        if (username.isBlank()) return "El nombre de usuario es requerido"
        if (username.length < 6) return "Debe tener al menos 6 caracteres"
        if (!username.matches("^[a-zA-Z0-9]+$".toRegex()))
            return "Sin espacios ni caracteres especiales"

        if (email.isBlank()) return "El correo es requerido"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return "Formato de correo inválido"

        if (password.isBlank()) return "La contraseña es requerida"
        if (password.length < 6) return "Mínimo 6 caracteres"

        val hasSpecial = password.any { !it.isLetterOrDigit() }
        if (!hasSpecial) return "Debe incluir un carácter especial"

        if (password != confirmPassword) return "Las contraseñas no coinciden"

        return null
    }

    fun handleRegister() {
        val errorMessage = validate()
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isLoading = true
            try {
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    rol = "null"
                )

                val response = RetrofitClient.apiService.registerUser(request)

                if (response.isSuccessful) {
                    response.body()?.let {
                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess(it, password)
                    } ?: Toast.makeText(context, "Respuesta vacía", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        errorBody?.let {
                            Json.decodeFromString<ErrorResponse>(it).error
                        }
                    } catch (e: Exception) {
                        "Error del servidor: ${response.code()}"
                    }
                    Toast.makeText(context, message ?: "Error desconocido", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Sin conexión al servidor", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Crear cuenta",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.primary
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            AuthTextField(
                value = username,
                onChange = { username = it },
                label = "Username",
                icon = Icons.Outlined.Person,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            AuthTextField(
                value = email,
                onChange = { email = it },
                label = "Email",
                icon = Icons.Outlined.Email,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            AuthTextField(
                value = password,
                onChange = { password = it },
                label = "Contraseña",
                icon = Icons.Outlined.Lock,
                isPassword = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            AuthTextField(
                value = confirmPassword,
                onChange = { confirmPassword = it },
                label = "Confirmar contraseña",
                icon = Icons.Outlined.Lock,
                isPassword = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(35.dp))

            if (isLoading) {
                CircularProgressIndicator(color = colors.primary)
            } else {
                Button(
                    onClick = { handleRegister() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        "Registrar",
                        color = colors.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primary
                ),
                enabled = !isLoading
            ) {
                Text(
                    "Volver",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
