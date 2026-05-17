package com.proyectopoo.petcareapp.ui.screen.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    var selectedRole by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun handleRoleUpdate() {
        if (selectedRole.isEmpty()) {
            Toast.makeText(context, "Por favor, selecciona un rol para continuar", Toast.LENGTH_SHORT).show()
            return
        }

        val apiRole = if (selectedRole == "Dueño") "propietario" else "gestor"

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

    Scaffold(
        containerColor = FondoClaro
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Elige tu rol",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = CafeOscuro,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¿Cómo usarás PetCare?",
                fontSize = 16.sp,
                color = CafeOscuro.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            RoleCard(
                title = "Dueño",
                description = "Busco a alguien que cuide de mi mejor amigo peludo",
                icon = Icons.Outlined.Pets,
                isSelected = selectedRole == "Dueño",
                isLoading = isLoading,
                onClick = { selectedRole = "Dueño" }
            )
            Spacer(modifier = Modifier.height(16.dp))
            RoleCard(
                title = "Cuidador",
                description = "Quiero ofrecer mis servicios para cuidar mascotas",
                icon = Icons.Outlined.Favorite,
                isSelected = selectedRole == "Cuidador",
                isLoading = isLoading,
                onClick = { selectedRole = "Cuidador" }
            )

            Spacer(modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(16.dp))


            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CafeOscuro.copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = CafeMedio,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rol único y permanente",
                            fontWeight = FontWeight.Bold,
                            color = CafeOscuro,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Una vez que elijas tu rol no podrás cambiarlo. Esto garantiza la seguridad y confianza en nuestra comunidad.",
                            color = CafeOscuro.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = CafeMedio, modifier = Modifier.size(48.dp))
            } else {
                Button(
                    onClick = { handleRoleUpdate() },
                    enabled = selectedRole.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CafeMedio,
                        disabledContainerColor = CafeClaro
                    ),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Text(
                        text = "Continuar",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}