package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.theme.BordeCampo
import com.proyectopoo.petcareapp.ui.theme.CafeMedio
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro
import com.proyectopoo.petcareapp.ui.theme.FondoCampo
import com.proyectopoo.petcareapp.ui.theme.TextoSuave

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }

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

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = {
                    Text("Nombre")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Nombre",
                        tint = CafeMedio
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
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
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))


            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                label = {
                    Text("Apellido")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Apellido",
                        tint = CafeMedio
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
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
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))


            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = {
                    Text("Correo")
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
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))


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
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
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
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))


            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = {
                    Text("Ubicación")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Ubicación",
                        tint = CafeMedio
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
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
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(35.dp))


            Button(
                onClick = onRegisterSuccess,
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

            Spacer(modifier = Modifier.height(14.dp))


            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CafeOscuro
                )
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