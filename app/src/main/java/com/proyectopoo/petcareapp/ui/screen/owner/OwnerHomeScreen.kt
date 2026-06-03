package com.proyectopoo.petcareapp.ui.screen.owner

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.entity.PetEntity

@Composable
fun OwnerHomeScreen(
    dogs: List<PetEntity>,
    onGoToCreate: () -> Unit,
    onEditPets: (PetEntity) -> Unit,
    onAddDog: () -> Unit,
    onGoToFeed: () -> Unit,
    onGoToOwnerProfile: () -> Unit,
    ownerId: Int
) {

    val scrollState = rememberScrollState()
    var selectedDogIndex by remember { mutableStateOf(0) }

    val safeIndex = if (dogs.isEmpty()) 0 else selectedDogIndex.coerceIn(0, dogs.lastIndex)
    val currentDog = dogs.getOrNull(safeIndex)

    val services = listOf(
        "Alojamiento" to Icons.Default.Home,
        "Guardería" to Icons.Default.WbSunny,
        "Paseo" to Icons.Default.DirectionsWalk,
        "Taxi" to Icons.Default.LocalTaxi,
        "Peluquería" to Icons.Default.ContentCut,
        "Visitante" to Icons.Default.HomeRepairService
    )

    val serviceDescriptions = mapOf(
        "Alojamiento" to "Estancia 24h o más en casa del cuidador",
        "Guardería" to "Cuidado de 8am a 8pm en casa del cuidador",
        "Paseo" to "El cuidador saca a pasear a tu perro",
        "Taxi" to "Traslado entre ubicaciones",
        "Peluquería" to "Servicio de grooming especializado",
        "Visitante" to "El cuidador va a tu casa a atenderlo"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {

            // ================= HEADER (más corto) =================
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        "Bienvenido",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        "¿Qué necesita tu mascota hoy?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // ================= PET CARD =================
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                ) {

                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    Icons.Default.Pets,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentDog?.name ?: "Agrega tu mascota",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = currentDog?.let {
                                        "${it.breed} · ${it.size}"
                                    } ?: "Sin información",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { currentDog?.let(onEditPets) },
                                enabled = currentDog != null
                            ) {
                                Icon(Icons.Default.Edit, null)
                            }
                        }

                        if (dogs.size > 1) {
                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { if (selectedDogIndex > 0) selectedDogIndex-- }
                                ) {
                                    Icon(Icons.Default.ArrowBack, null)
                                }

                                Text(
                                    text = "${safeIndex + 1}/${dogs.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(
                                    onClick = { if (selectedDogIndex < dogs.lastIndex) selectedDogIndex++ }
                                ) {
                                    Icon(Icons.Default.ArrowForward, null)
                                }
                            }
                        }
                    }
                }

                // ================= ADD DOG =================
                OutlinedButton(
                    onClick = onAddDog,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar mascota")
                }

                Text(
                    "Servicios disponibles",
                    style = MaterialTheme.typography.titleLarge
                )

                // ================= SERVICES GRID =================
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    services.chunked(2).forEach { row ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            row.forEach { (name, icon) ->

                                Card(
                                    onClick = onGoToCreate,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(150.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {

                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {

                                        Icon(
                                            icon,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )

                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.titleSmall
                                        )

                                        Text(
                                            text = serviceDescriptions[name] ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }

                            if (row.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                // ================= CTA =================
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "¿Buscas paseadores?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )

                            Text(
                                "Encuentra los mejores cerca de ti",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                        }

                        Button(
                            onClick = onGoToFeed,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Explorar", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}