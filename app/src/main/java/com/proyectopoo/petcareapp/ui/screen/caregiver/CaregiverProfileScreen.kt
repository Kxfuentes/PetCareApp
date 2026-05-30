package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.model.User
import com.proyectopoo.petcareapp.ui.components.UserBasicInfoCard

@Composable
fun CaregiverProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    user: User? = null
) {

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(24.dp)
    ) {

        Text(
            text = "Mi Perfil - Cuidador",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        UserBasicInfoCard(
            name = user?.username ?: "Nombre del Usuario",
            email = user?.email ?: "correo@ejemplo.com",
            role = "Cuidador Profesional"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colorScheme.outline, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            )
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Calificación: ★★★★☆ (4.8)", color = colorScheme.onSurface)
                Text("Servicios completados: 47", color = colorScheme.onSurface)
                Text("Disponibilidad: Disponible ahora", color = colorScheme.primary)
                Text("Ubicación: Managua, Nicaragua", color = colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Historial de Trabajos Realizados",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colorScheme.outline, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("• Cuidado de Luna (3 días) - $450", color = colorScheme.onSurface)
                Text("• Paseos semanales de Max - $320", color = colorScheme.onSurface)
                Text("• Hospedaje de Rocky - $600", color = colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            )
        ) {
            Text(
                "Gestionar Disponibilidad y Precios",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colorScheme.error),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorScheme.error
            )
        ) {
            Text(
                "Cerrar Sesión",
                fontWeight = FontWeight.Bold
            )
        }
    }
}