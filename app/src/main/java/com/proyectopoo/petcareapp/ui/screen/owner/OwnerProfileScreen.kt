package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.model.User
import com.proyectopoo.petcareapp.ui.components.UserBasicInfoCard

@Composable
fun OwnerProfileScreen(
    onBack: () -> Unit,         
    onLogout: () -> Unit,
    user: User? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Mi Perfil - Dueño",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        UserBasicInfoCard(
            name = user?.nombre ?: "Nombre del Usuario",
            email = user?.email ?: "correo@ejemplo.com",
            role = "Dueño de Mascotas"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Mis Mascotas y Servicios", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("• Max (Golden Retriever)")
                Text("• Luna (Gato)")
                Text("• Rocky (Pastor Alemán)")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Historial de Servicios Publicados", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("• Paseo de Max - 15 Mayo")
                Text("• Cuidado de Luna - 10 Mayo")
                Text("• Hospedaje de Rocky - 5 Mayo")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = { /* TODO: Editar perfil */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Editar Perfil")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar Sesión")
        }
    }
}