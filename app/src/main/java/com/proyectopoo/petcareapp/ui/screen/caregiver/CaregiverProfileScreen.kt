package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.model.User
import com.proyectopoo.petcareapp.ui.components.BadgeChip
import com.proyectopoo.petcareapp.ui.theme.*

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
    onManageAvailability: () -> Unit = {},
    badge: String? = null,
    noMolestar: Boolean = false,
    onToggleNoMolestar: (Boolean) -> Unit = {},
    onGoToStats: () -> Unit = {},
    onGoToNotifications: () -> Unit = {},
    onGoToSearch: () -> Unit = {},
    onGoToHelp: () -> Unit = {},
    onGoToSettings: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {

        // Tarjeta de Perfil
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
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = CafeMedio
                    )
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
                        color = TextoSuave
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Cuidador",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSuave
                    )
                    badge?.let {
                        Spacer(Modifier.height(6.dp))
                        BadgeChip(it)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Rendimiento - Dos tarjetas lado a lado
        Text(
            text = "Mi Rendimiento",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = CafeOscuro
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tarjeta Calificación
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Star, null, tint = CafeMedio, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "%.1f".format(rating),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = CafeOscuro
                    )
                    Text("Calificación", style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
                }
            }

            // Tarjeta Servicios Completados
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.AssignmentTurnedIn, null, tint = CafeMedio, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = completedServicesCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = CafeOscuro
                    )
                    Text(
                        "Servicios completados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSuave,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isOwnProfile) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "No molestar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Silencia las notificaciones push mientras esté activo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                    }
                    Switch(checked = noMolestar, onCheckedChange = onToggleNoMolestar)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (isOwnProfile) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    CaregiverProfileMenuRow(Icons.Default.BarChart, "Mis estadísticas", onGoToStats)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    CaregiverProfileMenuRow(Icons.Default.Notifications, "Notificaciones", onGoToNotifications)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    CaregiverProfileMenuRow(Icons.Default.Search, "Buscar", onGoToSearch)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    CaregiverProfileMenuRow(Icons.AutoMirrored.Filled.HelpOutline, "Ayuda", onGoToHelp)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    CaregiverProfileMenuRow(Icons.Default.Settings, "Configuración", onGoToSettings)
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onEditProfile,
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D5524))
            ) {
                Icon(Icons.Default.Edit, null)
                Spacer(Modifier.width(8.dp))
                Text("Edición de tu perfil")   // Texto cambiado
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogout,
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}

@Composable
private fun CaregiverProfileMenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(text, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}