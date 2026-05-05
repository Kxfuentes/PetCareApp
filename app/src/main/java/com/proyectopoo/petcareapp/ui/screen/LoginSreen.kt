package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.proyectopoo.petcareapp.ui.components.CampoLogin
import com.proyectopoo.petcareapp.ui.components.PantallaPetCare
import com.proyectopoo.petcareapp.ui.components.TituloPetCare
import com.proyectopoo.petcareapp.ui.theme.CafeClaro
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro

@Composable
fun LoginScreen(
    onRoleSelection: () -> Unit,
    onGoToRegister: () -> Unit = {}
) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    PantallaPetCare {
        Spacer(Modifier.height(60.dp))
        TituloPetCare()
        Text("Iniciar Sesión", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CafeOscuro)
        Spacer(Modifier.height(30.dp))

        CampoLogin("Usuario", user, "Ingrese su usuario", { user = it })
        CampoLogin("Contraseña", pass, "••••••", { pass = it }, esPassword = true)

        Button(
            onClick = { onRoleSelection() },
            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CafeClaro),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Entrar", color = CafeOscuro, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onGoToRegister) {
            Text("¿No tienes cuenta? Regístrate", color = CafeOscuro)
        }
    }
}