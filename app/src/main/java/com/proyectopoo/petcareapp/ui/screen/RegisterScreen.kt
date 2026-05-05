package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// IMPORTANTE: Solo importar de la nueva carpeta components
import com.proyectopoo.petcareapp.ui.components.PantallaPetCare
import com.proyectopoo.petcareapp.ui.components.TituloPetCare
import com.proyectopoo.petcareapp.ui.components.CampoLogin

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    PantallaPetCare {
        Spacer(Modifier.height(40.dp))
        TituloPetCare()

        CampoLogin("Usuario", username, "Ej: Juan123", { username = it })
        CampoLogin("Correo", email, "ejemplo@mail.com", { email = it })
        CampoLogin("Contraseña", password, "", { password = it }, esPassword = true)

        Button(
            onClick = onRegisterSuccess,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        TextButton(onClick = onBack) {
            Text("Volver")
        }
    }
}