package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.LocalUserRoleViewModel
import com.proyectopoo.petcareapp.ui.theme.CafeClaro
import com.proyectopoo.petcareapp.ui.theme.CafeMedio
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro
import com.proyectopoo.petcareapp.ui.theme.FondoCrema
import com.proyectopoo.petcareapp.ui.theme.TextoSuave

@Composable
fun LoginScreen(
    onRoleSelection: () -> Unit,
    onGoToRegister: () -> Unit = {}
) {
    val userRoleViewModel = LocalUserRoleViewModel.current
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
    ) {
        Text(
            text = "",
            fontSize = 120.sp,
            color = CafeClaro.copy(alpha = 0.12f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .align(Alignment.Center)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Pets, "Logo", tint = CafeOscuro, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("PetCare", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = CafeOscuro)
                }

                Spacer(modifier = Modifier.height(30.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        errorMessage = null
                    },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Outlined.Email, null, tint = CafeMedio) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = CafeMedio) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = CafeMedio)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        // VALIDACIÓN DE USUARIOS DEFAULT
                        when {
                            correo == "kelly@petcare.com" && password == "kelly123" -> {
                                userRoleViewModel.setRole(UserRole.OWNER)
                                onRoleSelection()
                            }
                            correo == "vanessa@petcare.com" && password == "vanessa123" -> {
                                userRoleViewModel.setRole(UserRole.CAREGIVER)
                                onRoleSelection()
                            }
                            correo.isBlank() || password.isBlank() -> {
                                errorMessage = "Por favor, completa todos los campos"
                            }
                            else -> {
                                // Si no son los usuarios default, permitimos el flujo normal
                                // pero sin asignar rol automáticamente (irá a RoleSection)
                                onRoleSelection()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CafeMedio)
                ) {
                    Text("Entrar", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(15.dp))

                TextButton(onClick = onGoToRegister) {
                    Text("¿No tienes cuenta? Regístrate", color = CafeOscuro)
                }

                Text(
                    text = "Tip: Usa kelly@petcare.com o vanessa@petcare.com",
                    fontSize = 10.sp,
                    color = TextoSuave,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}
