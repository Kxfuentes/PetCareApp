package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.model.User
import com.proyectopoo.petcareapp.ui.components.UserBasicInfoCard

@Composable
fun CaregiverProfileScreen(
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
            text = "Mi Perfil - Cuidador",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        UserBasicInfoCard(
            name = user?.nombre ?: "Nombre del Usuario",
            email = user?.email ?: "correo@ejemplo.com",
            role = "Cuidador Profesional"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Calificación: ★★★★☆ (4.8)")
                Text("Servicios completados: 47")
                Text("Disponibilidad: Disponible ahora")
                Text("Ubicación: Managua, Nicaragua")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Historial de Trabajos Realizados", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("• Cuidado de Luna (3 días) - $450")
                Text("• Paseos semanales de Max - $320")
                Text("• Hospedaje de Rocky - $600")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: Gestionar disponibilidad */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gestionar Disponibilidad y Precios")
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