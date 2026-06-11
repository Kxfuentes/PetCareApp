package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.ui.components.ActionCard

@Composable
fun CaregiverHomeScreen(
    onGoToServices: () -> Unit,
    ownerRequests: List<ServiceApplicationDetails>,
    onAcceptApplication: (Int) -> Unit,
    onRejectApplication: (Int) -> Unit,
    caregiverId: Int
) {
    var available by remember { mutableStateOf(true) }
    var showHeader by remember { mutableStateOf(true) } // ← Estado del Header agregado

    val scrollState = rememberScrollState()

    val nextCommitment = ownerRequests
        .filter { it.applicationStatus == ApplicationStatus.ACCEPTED }
        .minByOrNull { it.requestedDate ?: "" }

    val pendingRequests = ownerRequests.filter {
        it.applicationStatus == ApplicationStatus.PENDING
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {

        // Header Sección (dismissible de manera condicional)
        if (showHeader) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sección del Cuidador",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestiona tus servicios y solicitudes",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = { showHeader = false }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Tarjeta de Estado Actual
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Estado actual",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (available) "Disponible para trabajar" else "En descanso",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Switch(
                        checked = available,
                        onCheckedChange = { available = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Text(
                text = "Próximo compromiso",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            CommitmentCard(commitment = nextCommitment)

            Text(
                text = "Tu actividad",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    title = "Mis Servicios",
                    icon = Icons.Default.List,
                    onClick = onGoToServices,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "Dueños que solicitaron tus servicios",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (pendingRequests.isEmpty()) {
                Text(
                    text = "Aún no tienes solicitudes pendientes.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    pendingRequests.forEach { request ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = request.ownerName ?: "Dueño",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "${request.requestTitle} · ${request.petName ?: "Mascota"}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { onAcceptApplication(request.applicationId) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Aceptar")
                                    }
                                    OutlinedButton(
                                        onClick = { onRejectApplication(request.applicationId) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Rechazar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommitmentCard(commitment: ServiceApplicationDetails?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = "Compromiso",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = commitment?.let {
                        "${it.requestTitle} con ${it.petName ?: "mascota"}"
                    } ?: "Sin compromisos próximos",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Hora",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = commitment?.requestedDate ?: "Cuando aceptes una solicitud aparecerá aquí",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}