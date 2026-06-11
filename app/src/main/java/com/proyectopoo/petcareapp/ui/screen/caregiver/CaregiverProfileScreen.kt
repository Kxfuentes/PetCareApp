package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverProfileScreen(
    user: User?,
    caregiverId: Int,
    isOwnProfile: Boolean,
    completedServicesCount: Int,
    rating: Double,
    isLoading: Boolean,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    onManageAvailability: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {

        // Tarjeta de Perfil (idéntica al Owner)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = user?.username ?: "Cuidador",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = user?.email ?: "correo@ejemplo.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cuidador Profesional", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (isOwnProfile) {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Rendimiento
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Mi Rendimiento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            if (isOwnProfile) {
                Button(onClick = onManageAvailability, shape = RoundedCornerShape(20.dp)) {
                    Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Disponibilidad")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Calificación: ${"%.1f".format(rating)} ★",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Servicios completados: $completedServicesCount",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (isOwnProfile) {
            Button(
                onClick = onEditProfile,
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Edit, null)
                Spacer(Modifier.width(8.dp))
                Text("Editar Perfil")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogout,
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}