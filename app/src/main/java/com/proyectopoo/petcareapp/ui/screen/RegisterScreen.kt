package com.proyectopoo.petcareapp.ui.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.data.network.ErrorResponse
import com.proyectopoo.petcareapp.data.network.RegisterRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.ui.theme.BordeCampo
import com.proyectopoo.petcareapp.ui.theme.CafeMedio
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro
import com.proyectopoo.petcareapp.ui.theme.FondoCampo
import com.proyectopoo.petcareapp.ui.theme.TextoSuave
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados para los campos requeridos por el servicio
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }

    // Función de validación local
    fun validate(): String? {
        if (username.isBlank()) return "El nombre de usuario es requerido"
        
        // Validación de username: mínimo 6 caracteres, sin espacios ni caracteres especiales
        if (username.length < 6) return "El nombre de usuario debe tener al menos 6 caracteres"
        if (!username.matches("^[a-zA-Z0-9]+$".toRegex())) {
            return "El nombre de usuario no puede contener espacios ni caracteres especiales"
        }

        if (email.isBlank()) return "El correo es requerido"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Formato de correo inválido"
        if (password.isBlank()) return "La contraseña es requerida"
        if (password != confirmPassword) return "Las contraseñas no coinciden"
        return null
    }

    // Lógica de registro e integración con el servicio
    fun handleRegister() {
        val errorMessage = validate()
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.apiService.registerUser(
                    RegisterRequest(
                        username = username,
                        email = email,
                        password = password,
                        rol = null // El rol se definirá en un paso posterior
                    )
                )

                if (response.isSuccessful) {
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    onRegisterSuccess()
                } else {
                    // Manejo de errores controlados del servicio (ej: campos faltantes, usuario duplicado)
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        if (errorBody != null) {
                            Json.decodeFromString<ErrorResponse>(errorBody).error
                        } else {
                            "Error desconocido del servidor"
                        }
                    } catch (e: Exception) {
                        "Error del servidor: ${response.code()}"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                // Error de conexión (offline, servidor apagado, etc.)
                Toast.makeText(context, "No se pudo conectar con el servidor. Verifica tu conexión.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // Otros errores inesperados
                Toast.makeText(context, "Ocurrió un error inesperado: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Crear cuenta",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = TextoSuave
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

            // Username (Requerido)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Person, contentDescription = null, tint = CafeMedio)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Email (Requerido)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Email, contentDescription = null, tint = CafeMedio)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Password (Requerido)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = CafeMedio)
                },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Confirmar Password (Validación local)
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = CafeMedio)
                },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(35.dp))

            if (isLoading) {
                CircularProgressIndicator(color = CafeMedio)
            } else {
                Button(
                    onClick = { handleRegister() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CafeMedio
                    )
                ) {
                    Text(
                        text = "Registrar",
                        color = Color.White,
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
                    contentColor = CafeOscuro
                ),
                enabled = !isLoading
            ) {
                Text(
                    text = "Volver",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = FondoCampo,
    unfocusedContainerColor = FondoCampo,
    disabledContainerColor = FondoCampo,
    focusedBorderColor = CafeMedio,
    unfocusedBorderColor = BordeCampo,
    focusedLabelColor = CafeMedio,
    unfocusedLabelColor = CafeOscuro,
    cursorColor = CafeOscuro,
    focusedTextColor = CafeOscuro,
    unfocusedTextColor = CafeOscuro
)
