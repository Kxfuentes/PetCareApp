package com.proyectopoo.petcareapp.ui.screen.caregiver

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.proyectopoo.petcareapp.R
import com.proyectopoo.petcareapp.data.local.entity.ApplicationStatus
import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import com.proyectopoo.petcareapp.data.network.EmergenciaRequest
import com.proyectopoo.petcareapp.data.network.EvidenciaTipo
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.data.network.ValoracionDuranteRequest
import com.proyectopoo.petcareapp.location.LocationReporter
import com.proyectopoo.petcareapp.ui.components.EmergencyReportDialog
import com.proyectopoo.petcareapp.ui.components.EvidenciaCaptureDialog
import com.proyectopoo.petcareapp.ui.components.EvidenciaThumbnails
import com.proyectopoo.petcareapp.ui.components.ReactionButtonsRow
import com.proyectopoo.petcareapp.ui.components.SkeletonList
import com.proyectopoo.petcareapp.ui.components.StarRatingInput
import com.proyectopoo.petcareapp.ui.components.retryPendingEvidencias
import com.proyectopoo.petcareapp.util.abrirNavegacion
import com.proyectopoo.petcareapp.util.compartirSolicitud
import kotlinx.coroutines.launch

/** Prompt de evidencia pendiente (Bloque 8): qué solicitud/tipo, y qué hacer al cerrarlo. */
private data class EvidenciaPrompt(
    val serviceRequestId: Int,
    val tipo: String,
    val afterAction: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverHomeScreen(
    onGoToServices: () -> Unit,
    ownerRequests: List<ServiceApplicationDetails>,
    scheduledServices: List<ServiceApplicationDetails>,
    onAcceptApplication: (Int) -> Unit,
    onRejectApplication: (Int) -> Unit,
    onCompleteAndRate: (ServiceApplicationDetails, Double, String) -> Unit,
    onCancelService: (ServiceApplicationDetails) -> Unit = {},
    onScheduledClick: (ServiceApplicationDetails) -> Unit = {},
    onOpenChat: (ServiceApplicationDetails) -> Unit = {},
    onGoToCalendar: () -> Unit = {},
    onGoToLostPetAlerts: () -> Unit = {},
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    // Cuando se vuelve desde CalendarioScreen (Bloque 9) tras tocar un servicio, este id abre
    // directamente su diálogo de detalle (reutilizando el diálogo existente en vez de duplicarlo).
    initialFocusServiceRequestId: Int? = null,
    onFocusHandled: () -> Unit = {},
    caregiverId: Int
) {
    val context = LocalContext.current
    var available by remember { mutableStateOf(true) }
    var showHeader by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val pendingRequests = ownerRequests.filter {
        it.applicationStatus == ApplicationStatus.PENDING &&
            it.initiatedBy == com.proyectopoo.petcareapp.data.local.entity.ApplicationInitiator.OWNER
    }
    val acceptedRequests = scheduledServices.filter { it.applicationStatus == ApplicationStatus.ACCEPTED }
    val waitingOwnerConfirmation = ownerRequests.filter { it.applicationStatus == ApplicationStatus.DONE_BY_CAREGIVER }

    // Mientras el cuidador tiene un Taxi/Paseo ACCEPTED activo, reporta su GPS en vivo (POST
    // .../ubicacion) para que el dueño lo vea en SeguimientoMapaScreen. Se arranca/detiene con
    // el ciclo de vida de este composable, igual que MainActivity conecta/desconecta su
    // PetCareWebSocketClient en un DisposableEffect.
    val trackingRequestId = acceptedRequests
        .firstOrNull { it.serviceTypeName.equals("Taxi", ignoreCase = true) || it.serviceTypeName.equals("Paseo", ignoreCase = true) }
        ?.serviceRequestId
    val locationReporter = remember { LocationReporter(context) }
    val locationReporterScope = rememberCoroutineScope()

    DisposableEffect(trackingRequestId) {
        if (trackingRequestId != null) {
            locationReporter.start(trackingRequestId, locationReporterScope)
        } else {
            locationReporter.stop()
        }
        onDispose { locationReporter.stop() }
    }
    val nextCommitment = acceptedRequests.minByOrNull { it.requestedDate ?: "" }
    var requestToRate by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var requestToDetails by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var ratingScore by remember { mutableStateOf(5f) }
    var ratingComment by remember { mutableStateOf("") }
    var emergencyDialogTarget by remember { mutableStateOf<ServiceApplicationDetails?>(null) }
    var isReportingEmergency by remember { mutableStateOf(false) }
    val actionsScope = rememberCoroutineScope()
    val actionsSnackbarHostState = remember { SnackbarHostState() }

    // Evidencia foto antes/después (Bloque 8): prompt pendiente (tipo + solicitud) y qué hacer
    // al terminar (subir/encolar/omitir nunca bloquea, ver EvidenciaCaptureDialog.kt).
    var evidenciaPrompt by remember { mutableStateOf<EvidenciaPrompt?>(null) }

    // Reintenta subir evidencia encolada localmente (sin conexión al capturarla) apenas el
    // dispositivo recupera conectividad, mismo patrón que ChatScreen.kt (Bloque 5).
    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                actionsScope.launch { retryPendingEvidencias(context) }
            }
        }
        runCatching { connectivityManager?.registerDefaultNetworkCallback(callback) }
        onDispose { runCatching { connectivityManager?.unregisterNetworkCallback(callback) } }
    }
    LaunchedEffect(Unit) { retryPendingEvidencias(context) }

    // Al volver desde el calendario (Bloque 9) con un servicio específico para abrir.
    LaunchedEffect(initialFocusServiceRequestId, scheduledServices, ownerRequests) {
        val targetId = initialFocusServiceRequestId ?: return@LaunchedEffect
        val match = scheduledServices.find { it.serviceRequestId == targetId }
            ?: ownerRequests.find { it.serviceRequestId == targetId }
        if (match != null) {
            requestToDetails = match
            onFocusHandled()
        }
    }
    val navAppNotInstalledMessage = stringResource(R.string.nav_app_not_installed)
    val shareRequest: (Int) -> Unit = { requestId ->
        if (!compartirSolicitud(context, requestId)) {
            actionsScope.launch {
                actionsSnackbarHostState.showSnackbar("No hay ninguna app disponible para compartir")
            }
        }
    }
    var isSendingReaction by remember { mutableStateOf(false) }
    val sendReaction: (Int, String) -> Unit = { requestId, tipoReaccion ->
        if (!isSendingReaction) {
            actionsScope.launch {
                isSendingReaction = true
                val response = runCatching {
                    RetrofitClient.apiService.valorarDurante(
                        id = requestId,
                        request = ValoracionDuranteRequest(usuarioId = caregiverId, tipoReaccion = tipoReaccion)
                    )
                }.getOrNull()
                isSendingReaction = false
                if (response?.isSuccessful == true) {
                    actionsSnackbarHostState.showSnackbar("Reacción enviada")
                } else {
                    actionsSnackbarHostState.showSnackbar("No se pudo enviar la reacción")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        if (showHeader) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Sección de cuidador",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Gestiona tus servicios y solicitudes",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IconButton(onClick = onGoToLostPetAlerts) {
                        Icon(Icons.Default.Campaign, contentDescription = "Mascotas perdidas cerca", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onGoToCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { showHeader = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        Column(
            Modifier.padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp))
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Estado actual", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (available) "Disponible para trabajar" else "En descanso",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
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

            SectionLabel("Próximo compromiso")
            CommitmentCard(commitment = nextCommitment)

            Card(
                onClick = onGoToServices,
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.List, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Mis servicios",
                        Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            SectionLabel("Dueños que solicitaron tus servicios")
            if (isLoading && pendingRequests.isEmpty()) {
                SkeletonList(count = 2)
            } else if (pendingRequests.isEmpty()) {
                EmptyCaregiverCard("Aún no tienes solicitudes pendientes.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    pendingRequests.forEach { request ->
                        OwnerRequestCard(
                            request = request,
                            onDetails = { requestToDetails = request },
                            onAccept = {
                                // La aceptación (PENDING -> ACCEPTED) nunca se bloquea por la
                                // foto: se dispara primero y el prompt de evidencia es un paso
                                // adicional encima, no una condición previa (Bloque 8).
                                onAcceptApplication(request.applicationId)
                                evidenciaPrompt = EvidenciaPrompt(request.serviceRequestId, EvidenciaTipo.ANTES)
                            },
                            onReject = { onRejectApplication(request.applicationId) }
                        )
                    }
                }
            }

            if (acceptedRequests.isNotEmpty()) {
                SectionTitle("Servicios activos")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    acceptedRequests.forEach { request ->
                        ActiveServiceCard(
                            request = request,
                            onDetails = { requestToDetails = request },
                            onCancel = { onCancelService(request) },
                            onFinish = {
                                // El prompt "Después" se muestra antes del diálogo de
                                // calificación; al terminarlo (suba, encole u omita), recién
                                // ahí se abre "Calificar dueño" (nunca se bloquea, Bloque 8).
                                evidenciaPrompt = EvidenciaPrompt(request.serviceRequestId, EvidenciaTipo.DESPUES) {
                                    requestToRate = request
                                    ratingScore = 5f
                                    ratingComment = ""
                                }
                            }
                        )
                    }
                }
            }

            if (waitingOwnerConfirmation.isNotEmpty()) {
                SectionTitle("Pendientes de calificación")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    waitingOwnerConfirmation.forEach { request ->
                        WaitingConfirmationCard(request = request, onClick = { requestToDetails = request })
                    }
                }
            }
        }
    }
    }

    SnackbarHost(
        hostState = actionsSnackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter)
    )
    }

    requestToDetails?.let { request ->
        // El cuidador solo viaja hacia el dueño en "Visitante" (siempre solicitud abierta,
        // nunca desde una oferta anunciada), así que la ubicación propia de la solicitud
        // (request.latitude/longitude) ya es el destino correcto.
        val visitanteDestination = if (request.serviceTypeName.equals("Visitante", ignoreCase = true)) {
            request.latitude?.let { lat -> request.longitude?.let { lng -> lat to lng } }
        } else null

        // "En curso" == ServiceRequestStatus.ACCEPTED (convención de este proyecto: no existe
        // un valor literal "EN_PROGRESO"). El botón de emergencia solo aplica mientras el
        // servicio está activo.
        val isInProgress = request.requestStatus.name == "ACCEPTED"

        CaregiverServiceDetailsDialog(
            request = request,
            onDismiss = { requestToDetails = null },
            onChatClick = {
                requestToDetails = null
                onOpenChat(request)
            },
            onComoLlegarClick = visitanteDestination?.let { (lat, lng) ->
                {
                    abrirNavegacion(context, lat, lng) {
                        actionsScope.launch {
                            actionsSnackbarHostState.showSnackbar(navAppNotInstalledMessage)
                        }
                    }
                }
            },
            onEmergencyClick = if (isInProgress) {
                {
                    requestToDetails = null
                    emergencyDialogTarget = request
                }
            } else null,
            onShareClick = { shareRequest(request.serviceRequestId) },
            onReact = if (isInProgress) {
                { tipo -> sendReaction(request.serviceRequestId, tipo) }
            } else null,
            isReactingEnabled = !isSendingReaction,
            evidenciaContent = { EvidenciaThumbnails(serviceRequestId = request.serviceRequestId) }
        )
    }

    evidenciaPrompt?.let { prompt ->
        EvidenciaCaptureDialog(
            serviceRequestId = prompt.serviceRequestId,
            tipo = prompt.tipo,
            onFinished = {
                val afterAction = prompt.afterAction
                evidenciaPrompt = null
                afterAction()
            }
        )
    }

    requestToRate?.let { request ->
        AlertDialog(
            onDismissRequest = { requestToRate = null },
            title = { Text("Calificar dueño") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StarRatingInput(value = ratingScore, onValueChange = { ratingScore = it })
                    OutlinedTextField(
                        value = ratingComment,
                        onValueChange = { ratingComment = it },
                        label = { Text("Comentario opcional") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCompleteAndRate(request, ratingScore.toDouble(), ratingComment)
                        requestToRate = null
                    }
                ) {
                    Text("Guardar y finalizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { requestToRate = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    emergencyDialogTarget?.let { request ->
        EmergencyReportDialog(
            isSubmitting = isReportingEmergency,
            onDismiss = { if (!isReportingEmergency) emergencyDialogTarget = null },
            onSubmit = { tipo, descripcion ->
                actionsScope.launch {
                    isReportingEmergency = true
                    val response = runCatching {
                        RetrofitClient.apiService.reportarEmergencia(
                            EmergenciaRequest(
                                serviceRequestId = request.serviceRequestId,
                                reportedBy = caregiverId,
                                tipo = tipo,
                                descripcion = descripcion
                            )
                        )
                    }.getOrNull()
                    isReportingEmergency = false
                    if (response?.isSuccessful == true) {
                        emergencyDialogTarget = null
                        actionsSnackbarHostState.showSnackbar("Emergencia reportada")
                    } else {
                        actionsSnackbarHostState.showSnackbar("No se pudo reportar la emergencia. Intenta de nuevo.")
                    }
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun EmptyCaregiverCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CommitmentCard(commitment: ServiceApplicationDetails?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(82.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    commitment?.let {
                        "${it.serviceTypeName ?: it.requestTitle} con ${it.petNames ?: it.petName ?: "mascota"}"
                    } ?: "Sin compromisos próximos",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(commitment?.requestedDate ?: "Cuando aceptes una solicitud aparecerá aquí")
                }
                commitment?.startTime?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun EmptyStateCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, modifier = Modifier.padding(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OwnerRequestCard(
    request: ServiceApplicationDetails,
    onDetails: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(request.ownerName ?: "Dueño", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${request.serviceTypeName ?: request.requestTitle} · ${request.petNames ?: request.petName ?: "Mascota"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("Fecha: ${request.requestedDate ?: "--"}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text("Hora: ${request.startTime ?: "--"}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }
            OutlinedButton(
                onClick = onDetails,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Ver detalles", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.action_accept), fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.action_reject), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ScheduledCaregiverRow(
    request: ServiceApplicationDetails,
    onCancel: () -> Unit,
    onRate: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(58.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(request.ownerName ?: "Dueño", fontWeight = FontWeight.Bold)
                    Text(request.petNames ?: request.petName ?: "Mascota", fontWeight = FontWeight.Medium)
                    Text(
                        "${request.serviceTypeName ?: request.requestTitle} • ${request.requestedDate ?: "--"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Ver detalles", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = canCancelService(request.requestedDate, request.startTime),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Close, null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_cancel))
                }
                OutlinedButton(
                    onClick = onRate,
                    enabled = canRateService(request.requestedDate, request.startTime, request.endTime),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.StarBorder, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Calificar")
                }
            }
        }
    }
}

private enum class CaregiverDetailFieldAction { NONE, CALL, EMAIL }

private data class CaregiverDetailField(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val isError: Boolean = false,
    val action: CaregiverDetailFieldAction = CaregiverDetailFieldAction.NONE
)

@Composable
private fun CaregiverServiceDetailsDialog(
    request: ServiceApplicationDetails,
    onDismiss: () -> Unit,
    onChatClick: (() -> Unit)? = null,
    onComoLlegarClick: (() -> Unit)? = null,
    onEmergencyClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onReact: ((String) -> Unit)? = null,
    isReactingEnabled: Boolean = true,
    evidenciaContent: (@Composable () -> Unit)? = null
) {
    val title = request.serviceTypeName ?: request.requestTitle
    val fields = caregiverDetailFields(request)
    val notes = request.requestDescription
        ?.lineSequence()
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() && !it.contains(":") }
        ?.joinToString("\n")
        .orEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(78.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                caregiverServiceIcon(title),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)
                        ) {
                            Text(
                                text = if (request.applicationStatus == ApplicationStatus.ACCEPTED) "Aceptado" else "Pendiente",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (onShareClick != null) {
                        IconButton(onClick = onShareClick) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Compartir solicitud",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        fields.forEachIndexed { index, field ->
                            CaregiverDetailFieldRow(field)
                            if (index != fields.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 52.dp, top = 10.dp, bottom = 10.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }

                if (notes.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.AutoMirrored.Filled.Notes, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Notas", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                                Text(notes, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                evidenciaContent?.invoke()

                if (onComoLlegarClick != null) {
                    OutlinedButton(
                        onClick = onComoLlegarClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cómo llegar", fontWeight = FontWeight.Bold)
                    }
                }

                if (onChatClick != null) {
                    OutlinedButton(
                        onClick = onChatClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Chat", fontWeight = FontWeight.Bold)
                    }
                }

                if (onEmergencyClick != null) {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = onEmergencyClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reportar emergencia", fontWeight = FontWeight.Bold)
                    }
                }

                if (onReact != null) {
                    Spacer(Modifier.height(4.dp))
                    ReactionButtonsRow(
                        enabled = isReactingEnabled,
                        onReact = onReact,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ActiveServiceCard(
    request: ServiceApplicationDetails,
    onDetails: () -> Unit,
    onCancel: () -> Unit,
    onFinish: () -> Unit
) {
    Card(
        onClick = onDetails,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(serviceIcon(request.serviceTypeName ?: request.requestTitle))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(request.serviceTypeName ?: request.requestTitle, fontWeight = FontWeight.Bold)
                    Text(
                        "Con ${request.ownerName ?: "dueño"} · ${request.petNames ?: request.petName ?: "Mascota"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(onClick = onFinish, modifier = Modifier.weight(1f)) {
                    Text("Finalizar")
                }
            }
        }
    }
}

@Composable
private fun WaitingConfirmationCard(request: ServiceApplicationDetails, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Default.HourglassTop)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.serviceTypeName ?: request.requestTitle, fontWeight = FontWeight.Bold)
                Text(
                    "Esperando confirmación del dueño.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(request.applicationStatus)
        }
    }
}

@Composable
private fun CompletedServiceCard(request: ServiceApplicationDetails, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(Icons.Default.CheckCircle)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.serviceTypeName ?: request.requestTitle, fontWeight = FontWeight.Bold)
                Text(
                    "Finalizado con ${request.ownerName ?: "dueño"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(request.applicationStatus)
        }
    }
}

@Composable
private fun CaregiverDetailFieldRow(field: CaregiverDetailField) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = when (field.action) {
            CaregiverDetailFieldAction.CALL -> Modifier.clickable {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${field.value}")))
            }
            CaregiverDetailFieldAction.EMAIL -> Modifier.clickable {
                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${field.value}")))
            }
            CaregiverDetailFieldAction.NONE -> Modifier
        }
    ) {
        Surface(
            shape = CircleShape,
            color = if (field.isError) MaterialTheme.colorScheme.error.copy(alpha = .12f) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    field.icon,
                    contentDescription = null,
                    tint = if (field.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(field.label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Text(
                field.value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun caregiverDetailFields(request: ServiceApplicationDetails): List<CaregiverDetailField> {
    val descriptionDetails = parseCaregiverDescriptionDetails(request.requestDescription)
    // Telefono/email del dueno solo se muestran una vez que el servicio esta confirmado (Bloque 10).
    val contactoVisible = request.applicationStatus == ApplicationStatus.ACCEPTED ||
        request.applicationStatus == ApplicationStatus.DONE_BY_CAREGIVER ||
        request.applicationStatus == ApplicationStatus.COMPLETED
    return buildList {
        add(CaregiverDetailField("Dueño", request.ownerName ?: "Dueño", Icons.Default.Person))
        add(CaregiverDetailField("Mascota", request.petNames?.takeIf { it.isNotBlank() } ?: request.petName ?: "Mascota", Icons.Default.Pets))
        request.petBreed?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Raza", it, Icons.Default.Badge)) }
        request.petSize?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Tamaño", it, Icons.Default.Scale)) }
        add(CaregiverDetailField("Fecha", request.requestedDate ?: "Sin fecha", Icons.Default.CalendarToday))
        request.startTime?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Hora inicio", it, Icons.Default.AccessTime)) }
        request.endTime?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Hora fin", it, Icons.Default.Schedule)) }
        descriptionDetails["Ubicación"]?.let { add(CaregiverDetailField("Ubicación", it, Icons.Default.LocationOn)) }
        descriptionDetails["Dirección de recogida"]?.let { add(CaregiverDetailField("Recogida", it, Icons.Default.LocationOn)) }
        descriptionDetails["Dirección de destino"]?.let { add(CaregiverDetailField("Destino", it, Icons.Default.Place)) }
        descriptionDetails["Precio"]?.let { add(CaregiverDetailField("Precio", it, Icons.Default.Payments)) }
        if (contactoVisible) {
            request.ownerEmail?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Email", it, Icons.Default.Email, action = CaregiverDetailFieldAction.EMAIL)) }
            request.ownerPhone?.takeIf { it.isNotBlank() }?.let { add(CaregiverDetailField("Teléfono", it, Icons.Default.Phone, action = CaregiverDetailFieldAction.CALL)) }
        }
    }
}

private fun parseCaregiverDescriptionDetails(description: String?): Map<String, String> =
    description.orEmpty()
        .lineSequence()
        .map { it.trim() }
        .filter { it.contains(":") }
        .mapNotNull { line ->
            val label = line.substringBefore(":").trim()
            val value = line.substringAfter(":").trim()
            if (label.isBlank() || value.isBlank()) null else label to value
        }
        .toMap()

private fun caregiverServiceIcon(serviceName: String): ImageVector = when (serviceName) {
    "Alojamiento" -> Icons.Default.NightShelter
    "Guardería" -> Icons.Default.WbSunny
    "Paseo" -> Icons.AutoMirrored.Filled.DirectionsWalk
    "Taxi" -> Icons.Default.LocalTaxi
    "Peluquería" -> Icons.Default.ContentCut
    else -> Icons.Default.Pets
}

private const val CANCELLATION_WINDOW_MS_UI = 3L * 60L * 60L * 1000L
private const val ONE_HOUR_MS_UI = 60L * 60L * 1000L

private fun canCancelService(date: String?, startTime: String?): Boolean {
    val startMillis = parseServiceDateTime(date, startTime) ?: return true
    return System.currentTimeMillis() <= startMillis - CANCELLATION_WINDOW_MS_UI
}

private fun canRateService(date: String?, startTime: String?, endTime: String?): Boolean {
    val startMillis = parseServiceDateTime(date, startTime) ?: return false
    val endMillis = parseServiceDateTime(date, endTime) ?: (startMillis + ONE_HOUR_MS_UI)
    return System.currentTimeMillis() >= endMillis
}

private fun parseServiceDateTime(date: String?, time: String?): Long? {
    if (date.isNullOrBlank()) return null
    return try {
        val hasTime = !time.isNullOrBlank()
        val pattern = if (hasTime) "dd/MM/yyyy HH:mm" else "dd/MM/yyyy"
        val text = if (hasTime) "$date $time" else date
        java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault()).apply { isLenient = false }.parse(text)?.time
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun IconBubble(icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(46.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun StatusChip(status: Enum<*>) {
    val (text, color) = when (status.name.uppercase()) {
        "PENDING" -> "Pendiente" to Color(0xFFFF9800)
        "ACCEPTED" -> "Aceptado" to Color(0xFF4CAF50)
        "DONE_BY_CAREGIVER" -> "Por confirmar" to Color(0xFF2196F3)
        "COMPLETED" -> "Completado" to Color(0xFF607D8B)
        "REJECTED" -> "Rechazado" to Color(0xFFF44336)
        "CANCELLED" -> "Cancelado" to Color(0xFFF44336)
        else -> status.name.replaceFirstChar { it.uppercase() } to MaterialTheme.colorScheme.outline
    }

    AssistChip(
        onClick = {},
        label = { Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = Color.Black) },
        colors = AssistChipDefaults.assistChipColors(containerColor = color.copy(alpha = 0.1f)),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = color.copy(alpha = 0.8f),
            borderWidth = 1.dp
        ),
        shape = RoundedCornerShape(20.dp)
    )
}

private fun serviceIcon(serviceName: String): ImageVector {
    return when (serviceName.lowercase()) {
        "paseo" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "alojamiento" -> Icons.Default.Home
        "guardería", "guarderia" -> Icons.Default.WbSunny
        "taxi" -> Icons.Default.LocalTaxi
        "peluquería", "peluqueria" -> Icons.Default.ContentCut
        "visitante" -> Icons.Default.HomeRepairService
        else -> Icons.AutoMirrored.Filled.Assignment
    }
}

private fun statusText(status: String): String {
    return when (status.uppercase()) {
        "PENDING" -> "Pendiente"
        "ACCEPTED" -> "Aceptado"
        "DONE_BY_CAREGIVER" -> "Pendiente de confirmar"
        "COMPLETED" -> "Completado"
        "REJECTED" -> "Rechazado"
        "CANCELLED" -> "Cancelado"
        else -> status
    }
}

