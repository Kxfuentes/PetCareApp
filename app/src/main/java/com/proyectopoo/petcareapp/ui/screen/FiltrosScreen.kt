package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Resultado de aplicar los filtros: radio de búsqueda, tipo de servicio (null = todos) y calificación mínima. */
data class FiltrosResult(
    val radiusKm: Float,
    val serviceType: String?,
    val minRating: Float
)

/**
 * Filtros de búsqueda compartidos por el feed de dueños y de cuidadores: radio de distancia,
 * tipo de servicio y calificación mínima. Al aplicar, entrega el resultado al llamador
 * (quien decide cómo usarlo: consultando GET /api/solicitudes/buscar o filtrando localmente).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosScreen(
    serviceTypes: List<String>,
    initial: FiltrosResult? = null,
    onBack: () -> Unit,
    onApply: (FiltrosResult) -> Unit,
    onClear: () -> Unit = {}
) {
    var radius by remember { mutableStateOf(initial?.radiusKm ?: 15f) }
    var selectedType by remember { mutableStateOf(initial?.serviceType ?: "Todos") }
    var minRating by remember { mutableStateOf(initial?.minRating ?: 0f) }
    var expanded by remember { mutableStateOf(false) }

    val options = listOf("Todos") + serviceTypes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Filtros") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Column {
                Text(
                    "Radio de búsqueda: ${radius.toInt()} km",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = radius,
                    onValueChange = { radius = it },
                    valueRange = 1f..100f
                )
            }

            Column {
                Text("Tipo de servicio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedType = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Column {
                Text(
                    "Calificación mínima: ${"%.1f".format(minRating)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = minRating,
                    onValueChange = { minRating = it },
                    valueRange = 0f..5f,
                    steps = 9
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    onApply(
                        FiltrosResult(
                            radiusKm = radius,
                            serviceType = selectedType.takeIf { it != "Todos" },
                            minRating = minRating
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Aplicar", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    radius = 15f
                    selectedType = "Todos"
                    minRating = 0f
                    onClear()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Limpiar filtros")
            }
        }
    }
}
