package com.proyectopoo.petcareapp.ui.screen.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.data.network.ErrorResponse
import com.proyectopoo.petcareapp.data.network.RegisterRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.ui.theme.CafeClaro
import com.proyectopoo.petcareapp.ui.theme.CafeMedio
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro
import com.proyectopoo.petcareapp.ui.theme.FondoClaro
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSectionScreen(
    userId: Int,
    username: String,
    email: String,
    password: String,
    onOwnerSelected: () -> Unit,
    onCaregiverSelected: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedRole by remember { mutableStateOf("") } // "Dueño" o "Cuidador"
    var isLoading by remember { mutableStateOf(false) }

    fun handleRoleUpdate() {
        if (selectedRole.isEmpty()) {
            Toast.makeText(context, "Por favor, selecciona un rol para continuar", Toast.LENGTH_SHORT).show()
            return
        }

        // Se usa "propietario" y "cuidador" para coincidir con la lógica de negocio y backend esperado
        val apiRole = if (selectedRole == "Dueño") "propietario" else "cuidador"

        scope.launch {
            isLoading = true
            try {
                val request = RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    rol = apiRole
                )
                
                val response = RetrofitClient.apiService.updateUserRole(userId, request)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Rol asignado correctamente", Toast.LENGTH_SHORT).show()
                    if (apiRole == "propietario") {
                        onOwnerSelected()
                    } else {
                        onCaregiverSelected()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        if (errorBody != null) {
                            Json.decodeFromString<ErrorResponse>(errorBody).error ?: "Error al actualizar el rol"
                        } else {
                            "Error al actualizar el rol"
                        }
                    } catch (e: Exception) {
                        "Error del servidor: ${response.code()}"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Error de red: No se pudo contactar al servidor", Toast.LENGTH_LONG).show()
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
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Selecciona tu tipo de cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CafeOscuro
        )

        Spacer(modifier = Modifier.height(40.dp))

        RoleSelectionCard(
            title = "Dueño",
            description = "Busco a alguien que cuide de mi mejor amigo peludo",
            isSelected = selectedRole == "Dueño",
            isLoading = isLoading,
            onClick = { selectedRole = "Dueño" }
        )

        Spacer(modifier = Modifier.height(20.dp))

        RoleSelectionCard(
            title = "Soy Cuidador",
            description = "Quiero ofrecer mis servicios para cuidar mascotas",
            isSelected = selectedRole == "Cuidador",
            isLoading = isLoading,
            onClick = { selectedRole = "Cuidador" }
        )

        Spacer(modifier = Modifier.weight(1f))

        if (isLoading) {
            CircularProgressIndicator(color = CafeMedio)
            Spacer(modifier = Modifier.height(20.dp))
        } else {
            Button(
                onClick = { handleRoleUpdate() },
                enabled = selectedRole.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CafeMedio
                )
            ) {
                Text(
                    text = "Siguiente",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = { if (!isLoading) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) CafeMedio else CafeClaro.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CafeClaro.copy(alpha = 0.2f) else FondoClaro
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CafeOscuro
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 15.sp,
                color = CafeOscuro.copy(alpha = 0.8f)
            )
        }
    }
}
