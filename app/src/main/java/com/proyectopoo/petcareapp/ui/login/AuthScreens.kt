package com.proyectopoo.petcareapp.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FondoCrema = Color(0xFFFFF7E8)
private val FondoClaro = Color(0xFFFFFBF2)
private val CafeOscuro = Color(0xFF3B2514)
private val CafeMedio = Color(0xFFB87950)
private val CafeClaro = Color(0xFFD9A77F)
private val FondoCampo = Color(0xFFFFFCF5)
private val BordeCampo = Color(0xFFD6A57A)
private val TextoSuave = Color(0xFF8A6A55)

@Composable
fun LoginScreen(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    PantallaPetCare {
        Spacer(modifier = Modifier.height(70.dp))

        TituloPetCare()
        Text("Iniciar Sesión", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CafeOscuro)

        Spacer(modifier = Modifier.height(30.dp))

        CampoLogin(
            label = "Usuario",
            valor = username,
            placeholder = "Ingrese su usuario",
            onChange = onUsernameChange
        )

        CampoLogin(
            label = "Contraseña",
            valor = password,
            placeholder = "Ingrese su contraseña",
            onChange = onPasswordChange,
            esPassword = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CafeClaro,
                contentColor = CafeOscuro
            )
        ) {
            Text("Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        TextButton(onClick = onRegisterClick) {
            Text("¿No tienes cuenta? Regístrate", color = CafeOscuro)
        }
    }
}

@Composable
fun RegisterScreen(
    username: String,
    onUsernameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    PantallaPetCare {
        Spacer(modifier = Modifier.height(55.dp))

        TituloPetCare()
        Text("Crear Cuenta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CafeOscuro)

        Spacer(modifier = Modifier.height(25.dp))

        CampoLogin("Usuario", username, "Ingrese su usuario", onUsernameChange)

        CampoLogin(
            label = "Correo",
            valor = email,
            placeholder = "ejemplo@gmail.com",
            onChange = onEmailChange,
            keyboardType = KeyboardType.Email
        )

        CampoLogin(
            label = "Contraseña",
            valor = password,
            placeholder = "Ingrese su contraseña",
            onChange = onPasswordChange,
            esPassword = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CafeClaro,
                contentColor = CafeOscuro
            )
        ) {
            Text("Register", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        TextButton(onClick = onLoginClick) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = CafeOscuro)
        }
    }
}

@Composable
fun RolesScreen(
    onRoleSelected: (String) -> Unit
) {
    PantallaPetCare {
        Spacer(modifier = Modifier.height(65.dp))

        TituloPetCare()

        Text(
            text = "Elige tu rol",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = CafeOscuro
        )

        Spacer(modifier = Modifier.height(28.dp))

        RolCard(
            titulo = "Cuidador",
            descripcion = "Ofrece servicios de paseo, cuidado o asistencia para mascotas.",
            onClick = { onRoleSelected("Cuidador") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RolCard(
            titulo = "Dueño",
            descripcion = "Publica solicitudes y busca apoyo para el cuidado de tu mascota.",
            onClick = { onRoleSelected("Dueño") }
        )
    }
}

@Composable
private fun PantallaPetCare(
    contenido: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FondoCrema, FondoClaro)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-60).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(CafeMedio)
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-70).dp)
                .clip(CircleShape)
                .background(CafeOscuro)
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-70).dp, y = 70.dp)
                .clip(CircleShape)
                .background(CafeOscuro)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = contenido
        )
    }
}

@Composable
private fun TituloPetCare() {
    Text(
        text = "PetCare",
        fontSize = 52.sp,
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        color = CafeMedio
    )
}

@Composable
private fun CampoLogin(
    label: String,
    valor: String,
    placeholder: String,
    onChange: (String) -> Unit,
    esPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = CafeOscuro,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            placeholder = {
                Text(placeholder, color = TextoSuave)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            visualTransformation = if (esPassword) {
                PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = CafeOscuro,
                unfocusedTextColor = CafeOscuro,
                cursorColor = CafeOscuro,
                focusedContainerColor = FondoCampo,
                unfocusedContainerColor = FondoCampo,
                focusedBorderColor = CafeMedio,
                unfocusedBorderColor = BordeCampo
            )
        )

        Spacer(modifier = Modifier.height(14.dp))
    }
}

@Composable
private fun RolCard(
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = FondoCampo
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = titulo,
                color = CafeOscuro,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = descripcion,
                color = TextoSuave,
                fontSize = 15.sp
            )
        }
    }
}