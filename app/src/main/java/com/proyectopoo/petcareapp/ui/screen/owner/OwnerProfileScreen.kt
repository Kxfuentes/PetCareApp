package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.local.entity.ServiceRequestStatus
import com.proyectopoo.petcareapp.data.local.relation.ServiceRequestDetails
import com.proyectopoo.petcareapp.model.User

@Composable
fun OwnerProfileScreen(
    user: User?,
    dogs: List<PetEntity>,
    historyServices: List<ServiceRequestDetails>,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    onAddPet: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showFullHistory by remember { mutableStateOf(false) }
    val sortedHistory = historyServices.sortedByDescending { it.requestedDate }
    val displayedHistory = if (showFullHistory) sortedHistory else sortedHistory.take(3)
    val completedCount = historyServices.count { it.status == ServiceRequestStatus.COMPLETED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(110.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(user?.username ?: "Usuario", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        ProfileInfoLine(Icons.Default.Email, user?.email ?: "correo@ejemplo.com", allowWrap = true)
                        ProfileInfoLine(Icons.Default.Person, "Dueño")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFF9800), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("0.0", fontWeight = FontWeight.Bold)
                            Text(" (0 reseñas)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(Icons.Default.Pets, dogs.size.toString(), "Mascotas")
                        VerticalDivider(modifier = Modifier.height(46.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        StatItem(Icons.Default.EventAvailable, completedCount.toString(), "Servicios\nrealizados")
                        VerticalDivider(modifier = Modifier.height(46.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        StatItem(Icons.Default.Star, "0.0", "Puntuación")
                    }
                }
            }
        }

        SectionTitle(null, "Mis Mascotas")
        if (dogs.isEmpty()) {
            EmptyProfileCard("Aún no tienes mascotas", "Agrega a tu peludo amigo para verlo aquí.")
        } else {
            dogs.forEach { pet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(72.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp)) }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(pet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(pet.breed ?: pet.species ?: "Sin raza registrada", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            pet.size?.takeIf { it.isNotBlank() }?.let { size ->
                                Spacer(Modifier.height(5.dp))
                                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                                    Text(formatProfilePetSize(size), modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(Icons.Default.History, "Historial de Servicios")
            if (sortedHistory.size > 3) {
                TextButton(onClick = { showFullHistory = !showFullHistory }) { Text(if (showFullHistory) "Ver menos" else "Ver todos") }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            if (sortedHistory.isEmpty()) {
                EmptyProfileCard("Aún no tienes servicios completados", "Cuando se completen servicios de tus mascotas, aparecerán aquí.")
            } else {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    displayedHistory.forEachIndexed { index, service ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            ServiceBubble(service.serviceTypeName ?: service.title)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(service.title, fontWeight = FontWeight.Bold)
                                Text(listOfNotNull(service.petName, service.requestedDate).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            StatusPill("Completado", Color(0xFF607D8B))
                        }
                        if (index != displayedHistory.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }

        Button(onClick = onEditProfile, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Default.Edit, null)
            Spacer(Modifier.width(8.dp))
            Text("Editar Perfil", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
            Icon(Icons.Default.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable private fun ProfileInfoLine(icon: ImageVector, text: String, allowWrap: Boolean = false) { Row(verticalAlignment = Alignment.Top) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(text, modifier = if (allowWrap) Modifier.weight(1f) else Modifier, maxLines = if (allowWrap) 2 else 1) } }
@Composable private fun SectionTitle(icon: ImageVector?, text: String) { Row(verticalAlignment = Alignment.CenterVertically) { if (icon != null) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)) }; Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) } }
@Composable private fun StatItem(icon: ImageVector, value: String, label: String) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 78.dp)) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge); Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) } }
@Composable private fun EmptyProfileCard(title: String, subtitle: String) { Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Pets, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline); Spacer(Modifier.height(10.dp)); Text(title, fontWeight = FontWeight.Bold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center) } }
@Composable private fun ServiceBubble(service: String) { Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(serviceIconForProfile(service), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(25.dp)) } } }
@Composable private fun StatusPill(text: String, color: Color) { Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = .12f)) { Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) } }
private fun serviceIconForProfile(service: String): ImageVector = when (service.lowercase()) { "paseo" -> Icons.Default.DirectionsWalk; "guardería" -> Icons.Default.WbSunny; "taxi" -> Icons.Default.LocalTaxi; "peluquería" -> Icons.Default.ContentCut; "alojamiento" -> Icons.Default.Home; else -> Icons.Default.Assignment }
private fun formatProfilePetSize(size: String): String = if (size.contains("kg", true) || size.contains("tamaño", true)) size else "Tamaño $size"
