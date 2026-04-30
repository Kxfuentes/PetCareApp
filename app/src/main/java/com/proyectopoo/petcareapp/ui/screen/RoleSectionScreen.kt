package com.proyectopoo.petcareapp.ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoleSectionScreen(
    onGoToFeed: () -> Unit
) {

    var selectedRole by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Selecciona tu rol",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { selectedRole = "Dueño de Mascota" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dueño de Mascota")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { selectedRole = "Cuidador" },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cuidador")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedRole.isNotBlank()) {
            Text("Rol seleccionado: $selectedRole")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGoToFeed,
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedRole.isNotBlank()
        ) {
            Text("Continuar")
        }
    }
}