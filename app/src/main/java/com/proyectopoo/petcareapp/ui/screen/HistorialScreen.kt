package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.data.network.ServiceRequestDto

/**
 * Historial de solicitudes (COMPLETED / CANCELLED) para el usuario actual.
 * Llama a GET /api/solicitudes/historial?usuarioId=&role=
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    usuarioId: Int,
    role: String,
    onBack: () -> Unit
) {
    var completados by remember { mutableStateOf<List<ServiceRequestDto>>(emptyList()) }
    var cancelados by remember { mutableStateOf<List<ServiceRequestDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(usuarioId, role) {
        if (usuarioId <= 0) {
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        loadError = false
        try {
            val response = RetrofitClient.apiService.getHistorialSolicitudes(usuarioId, role)
            if (response.isSuccessful) {
                val all = response.body().orEmpty()
                completados = all.filter { it.status == "COMPLETED" }
                cancelados = all.filter { it.status == "CANCELLED" }
            } else {
                loadError = true
            }
        } catch (e: Exception) {
            loadError = true
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Completados") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Cancelados") }
                )
            }

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                loadError -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No se pudo cargar el historial. Revisa tu conexión.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    val list = if (selectedTab == 0) completados else cancelados
                    if (list.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                if (selectedTab == 0) "Aún no tienes servicios completados." else "No tienes servicios cancelados.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { request ->
                                HistorialCard(request)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorialCard(request: ServiceRequestDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(request.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            request.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Text(
                request.requestedDate?.let { "Fecha: $it" } ?: "Sin fecha",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
