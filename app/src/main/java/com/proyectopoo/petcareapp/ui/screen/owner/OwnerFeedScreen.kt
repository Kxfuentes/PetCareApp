package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyectopoo.petcareapp.model.Cuidador
import com.proyectopoo.petcareapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OwnerFeedScreen(
    onGoToCaregiverProfile: (Int) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme

    // Estados del ViewModel
    val locationState by viewModel.locationSearchState.collectAsState()
    val caregiverState by viewModel.caregiverState.collectAsState()

    var showLocationResults by remember { mutableStateOf(false) }
    var mostrarCercanos by remember { mutableStateOf(false) }

    val tiposServicio = listOf(
        "Todos", "Alojamiento", "Guardería", "Paseo", "Taxi", "Peluquería", "Visitante"
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") }

    LaunchedEffect(Unit) {
        viewModel.loadAllCaregivers()
    }

    LaunchedEffect(locationState.selectedLocation) {
        if (locationState.selectedLocation != null) {
            showLocationResults = false
            val lat = locationState.selectedLocation!!.lat.toDoubleOrNull()
            val lon = locationState.selectedLocation!!.lon.toDoubleOrNull()
            if (lat != null && lon != null) {
                viewModel.searchCaregiversNearby(lat, lon, 10.0)
                mostrarCercanos = true
            }
        }
    }

    // Determinar qué cuidadores mostrar
    val cuidadoresAMostrar = if (mostrarCercanos && caregiverState.nearbyCaregivers.isNotEmpty()) {
        caregiverState.nearbyCaregivers
    } else if (mostrarCercanos && caregiverState.nearbyCaregivers.isEmpty() && !caregiverState.isLoading) {
        emptyList()
    } else {
        // Filtro por tipo de servicio
        if (selectedFilter == "Todos") {
            caregiverState.caregivers
        } else {
            caregiverState.caregivers.filter { it.servicios.contains(selectedFilter) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuidadores disponibles") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground
                )
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Text(
                text = "Encuentra cuidadores cerca de ti",
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = locationState.query,
                onValueChange = {
                    viewModel.updateSearchQuery(it)
                    showLocationResults = it.isNotEmpty()
                    if (it.isEmpty()) {
                        mostrarCercanos = false
                        viewModel.clearCaregiverSearch()
                    }
                },
                label = { Text("Tu ubicación") },
                placeholder = { Text("Ej: Tu dirección actual") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.surfaceVariant,
                    unfocusedContainerColor = colorScheme.surfaceVariant,
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline
                ),
                trailingIcon = {
                    if (locationState.query.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.clearSearchResults()
                            viewModel.clearCaregiverSearch()
                            showLocationResults = false
                            mostrarCercanos = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            )

            if (showLocationResults && locationState.results.isNotEmpty() && !locationState.isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant
                    )
                ) {
                    LazyColumn {
                        items(locationState.results) { result ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        result.display_name,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = "${result.lat}, ${result.lon}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = colorScheme.primary
                                    )
                                },
                                modifier = Modifier.clickable {
                                    viewModel.selectLocation(result)
                                    showLocationResults = false
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            // Mostrar estado de carga de ubicación
            if (locationState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            // Mostrar ubicación seleccionada
            if (locationState.selectedLocation != null && !showLocationResults) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buscando cuidadores cerca de: ${locationState.selectedLocation!!.display_name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Mostrar mensaje de "No hay resultados"
            if (mostrarCercanos && caregiverState.nearbyCaregivers.isEmpty() && !caregiverState.isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay cuidadores disponibles cerca de esta ubicación",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Prueba con otra ubicación o amplía el radio de búsqueda",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!mostrarCercanos || caregiverState.nearbyCaregivers.isEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedFilter,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filtrar por servicio") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surfaceVariant,
                            unfocusedContainerColor = colorScheme.surfaceVariant,
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        tiposServicio.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = {
                                    selectedFilter = tipo
                                    expanded = false
                                    // Limpiar resultados de búsqueda por ubicación
                                    mostrarCercanos = false
                                    viewModel.clearCaregiverSearch()
                                    viewModel.clearSearchResults()
                                    // Si no es "Todos", filtrar por servicio
                                    if (tipo != "Todos") {
                                        viewModel.searchCaregiversByService(tipo)
                                    } else {
                                        viewModel.loadAllCaregivers()
                                    }
                                }
                            )
                        }
                    }
                }

                // Botón para resetear filtro
                if (selectedFilter != "Todos") {
                    TextButton(
                        onClick = {
                            selectedFilter = "Todos"
                            mostrarCercanos = false
                            viewModel.clearCaregiverSearch()
                            viewModel.clearSearchResults()
                            viewModel.loadAllCaregivers()
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar filtro")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar filtro")
                    }
                }
            } else {
                // Botón para volver a la lista completa
                TextButton(
                    onClick = {
                        mostrarCercanos = false
                        viewModel.clearCaregiverSearch()
                        viewModel.clearSearchResults()
                        selectedFilter = "Todos"
                        viewModel.loadAllCaregivers()
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver todos los cuidadores")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mostrar error si hay
            if (caregiverState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = caregiverState.error!!,
                            color = colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Mostrar indicador de carga
            if (caregiverState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Buscando cuidadores...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Lista de cuidadores
            if (!caregiverState.isLoading) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Mostrar contador de resultados si está buscando por ubicación
                    if (mostrarCercanos && caregiverState.nearbyCaregivers.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Se encontraron ${caregiverState.totalFound} cuidadores cerca de ti",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    items(cuidadoresAMostrar) { cuidador ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = colorScheme.primary,
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = cuidador.nombre.first().toString(),
                                                color = colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 22.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = cuidador.nombre,
                                                style = MaterialTheme.typography.titleMedium
                                            )

                                            // Indicador de "Cerca" si está en modo búsqueda
                                            if (mostrarCercanos) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                AssistChip(
                                                    onClick = { },
                                                    label = {
                                                        Text(
                                                            "Cerca",
                                                            fontSize = 10.sp
                                                        )
                                                    },
                                                    colors = AssistChipDefaults.assistChipColors(
                                                        containerColor = colorScheme.primary,
                                                        labelColor = colorScheme.onPrimary
                                                    ),
                                                    modifier = Modifier.height(24.dp)
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Star,
                                                null,
                                                tint = colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "${cuidador.rating} (${cuidador.reviews})",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Ubicación",
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cuidador.ubicacion,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Precio: ${cuidador.precio}",
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    cuidador.servicios.forEach { servicio ->
                                        AssistChip(
                                            onClick = { },
                                            label = { Text(servicio) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = colorScheme.secondaryContainer,
                                                labelColor = colorScheme.onSecondaryContainer
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "\"${cuidador.review}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                OutlinedButton(
                                    onClick = { onGoToCaregiverProfile(cuidador.id.toIntOrNull() ?: 0) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Ver perfil completo", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}