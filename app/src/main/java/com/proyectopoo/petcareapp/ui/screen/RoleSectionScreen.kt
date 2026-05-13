package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.proyectopoo.petcareapp.LocalUserRoleViewModel
import com.proyectopoo.petcareapp.ui.theme.CafeClaro
import com.proyectopoo.petcareapp.ui.theme.CafeMedio
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro
import com.proyectopoo.petcareapp.ui.theme.FondoClaro

@Composable
fun RoleSectionScreen(
    onOwnerSelected: () -> Unit,
    onCaregiverSelected: () -> Unit
) {
    val userRoleViewModel = LocalUserRoleViewModel.current
    val registeredRole by userRoleViewModel.registeredRole.collectAsStateWithLifecycle()
    
    var selectedRole by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "Selecciona tu tipo de cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CafeOscuro
        )

        Spacer(modifier = Modifier.height(35.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    selectedRole = "Dueño"
                    errorMessage = null
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    if (selectedRole == "Dueño")
                        CafeClaro
                    else
                        FondoClaro
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = CafeClaro,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {

                Text(
                    text = "Dueño",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeOscuro
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Busco a alguien que cuide de mi mejor amigo peludo",
                    fontSize = 16.sp,
                    color = CafeOscuro
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    selectedRole = "Cuidador"
                    errorMessage = null
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    if (selectedRole == "Cuidador")
                        CafeClaro
                    else
                        FondoClaro
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = CafeClaro,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {

                Text(
                    text = "Soy Cuidador",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeOscuro
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Quiero ofrecer mis servicios para cuidar mascotas",
                    fontSize = 16.sp,
                    color = CafeOscuro
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val chosenRole = if (selectedRole == "Dueño") UserRole.OWNER else UserRole.CAREGIVER
                
                // VALIDACIÓN: El rol seleccionado debe coincidir con el registrado
                if (registeredRole != null && chosenRole != registeredRole) {
                    errorMessage = "Error: Tu cuenta está registrada como ${if (registeredRole == UserRole.OWNER) "Dueño" else "Cuidador"}. No puedes entrar con otro rol."
                } else {
                    if (chosenRole == UserRole.OWNER) {
                        onOwnerSelected()
                    } else {
                        onCaregiverSelected()
                    }
                }
            },
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

        Spacer(modifier = Modifier.height(20.dp))
    }
}
