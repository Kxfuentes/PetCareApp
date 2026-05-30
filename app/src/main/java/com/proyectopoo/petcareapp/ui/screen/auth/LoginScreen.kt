package com.proyectopoo.petcareapp.ui.screen.auth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.LocalUserRoleViewModel

@Composable
fun LoginScreen(
    onLoginClick: (String, String, Boolean) -> Unit,
    onGoToRegister: () -> Unit = {},
    onGoToPasswordRecovery: () -> Unit
) {
    val userRoleViewModel = LocalUserRoleViewModel.current
    val colors = MaterialTheme.colorScheme

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rememberSession by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .align(Alignment.Center)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Pets,
                        contentDescription = "Logo",
                        tint = colors.primary,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "PetCare",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        errorMessage = null
                    },
                    label = { Text("Correo electrónico") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, null, tint = colors.primary)
                    },
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
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, null, tint = colors.primary)
                    },
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = null,
                                tint = colors.primary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = colors.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberSession,
                        onCheckedChange = { rememberSession = it }
                    )

                    Text(
                        text = "Recordarme",
                        color = colors.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        if (correo.isNotBlank() && password.isNotBlank()) {
                            userRoleViewModel.setRegisteredRole(null)
                            onLoginClick(correo, password, rememberSession)
                        } else {
                            errorMessage = "Por favor, completa todos los campos"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        "Entrar",
                        color = colors.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                TextButton(onClick = onGoToRegister) {
                    Text(
                        "¿No tienes cuenta? Regístrate",
                        color = colors.primary
                    )
                }

                TextButton(onClick = onGoToPasswordRecovery) {
                    Text(
                        "¿Olvidaste tu contraseña? Recupérala aquí",
                        color = colors.primary
                    )
                }
            }
        }
    }
}