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

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
    ) {

        Text(
            text = "🐾",
            fontSize = 120.sp,
            color = CafeClaro.copy(alpha = 0.12f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(20.dp)
        )

        Text(
            text = "\uD83D\uDC36",
            fontSize = 140.sp,
            color = TextoSuave.copy(alpha = 0.10f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .align(Alignment.Center)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Pets,
                        contentDescription = "PetCare",
                        tint = CafeOscuro,
                        modifier = Modifier.size(34.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "PetCare",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeOscuro
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))


                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = {
                        Text("Correo electrónico")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Correo",
                            tint = CafeMedio
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedBorderColor = CafeMedio,
                        unfocusedBorderColor = CafeClaro,
                        focusedLabelColor = CafeMedio,
                        cursorColor = CafeOscuro
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))


                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text("Contraseña")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Contraseña",
                            tint = CafeMedio
                        )
                    },
                    trailingIcon = {

                        val image =
                            if (passwordVisible)
                                Icons.Outlined.Visibility
                            else
                                Icons.Outlined.VisibilityOff

                        IconButton(
                            onClick = {
                                passwordVisible = !passwordVisible
                            }
                        ) {

                            Icon(
                                imageVector = image,
                                contentDescription = "Mostrar contraseña",
                                tint =
                                    if (passwordVisible)
                                        CafeMedio
                                    else
                                        CafeClaro
                            )
                        }
                    },
                    visualTransformation =
                        if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedBorderColor = CafeMedio,
                        unfocusedBorderColor = CafeClaro,
                        focusedLabelColor = CafeMedio,
                        cursorColor = CafeOscuro
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(30.dp))


                Button(
                    onClick = { onRoleSelection() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CafeMedio
                    )
                ) {

                    Text(
                        text = "Entrar",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))


                TextButton(
                    onClick = onGoToRegister
                ) {

                    Text(
                        text = "¿No tienes cuenta? Regístrate",
                        color = CafeOscuro,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}