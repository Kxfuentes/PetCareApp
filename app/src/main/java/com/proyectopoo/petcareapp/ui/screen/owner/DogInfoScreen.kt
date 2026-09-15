package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.R
import com.proyectopoo.petcareapp.data.local.entity.PetEntity
import com.proyectopoo.petcareapp.data.network.AlertaPerdidaDto
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.ui.components.ReportarMascotaPerdidaDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de perfil de una mascota: crea una mascota nueva, o -- cuando se abre con
 * [editingDog] no nulo -- funciona como el "perfil" de una mascota existente, con pestañas para
 * los datos básicos y el expediente médico (Bloque 11), y el flujo de alerta de mascota perdida
 * (Bloque 12, botones en la pestaña "Datos").
 *
 * La pestaña de Expediente Médico solo aparece al editar una mascota existente (se necesita un
 * petId real del backend); al crear una mascota nueva se muestra únicamente el formulario
 * original.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DogInfoScreen(
    editingDog: PetEntity? = null,
    currentUserId: Int = -1,
    onFinish: (name: String, breed: String, size: String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val emptyFieldsMessage = stringResource(R.string.error_empty_fields)

    // Clave segura para reiniciar el estado al cambiar de mascota
    val key = editingDog?.petId ?: -1

    var isSaving by remember { mutableStateOf(false) }
    var dogName by remember(key) { mutableStateOf(editingDog?.name.orEmpty()) }
    var breed by remember(key) { mutableStateOf(editingDog?.breed.orEmpty()) }
    var selectedSize by remember(key) { mutableStateOf(editingDog?.size.orEmpty()) }

    var breedSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var breedMenuExpanded by remember { mutableStateOf(false) }

    // Autocompletado de razas: consulta el backend (Dog CEO API) 300ms despues de la ultima tecla.
    LaunchedEffect(breed) {
        delay(300)
        val result = runCatching {
            RetrofitClient.apiService.searchDogBreeds(breed.takeIf { it.isNotBlank() })
        }.getOrNull()
        if (result?.isSuccessful == true) {
            breedSuggestions = result.body()?.razas.orEmpty()
            breedMenuExpanded = breedSuggestions.isNotEmpty()
        }
    }

    val sizes = listOf("XS (1-5 kg)", "S (5-10 kg)", "M (10-20 kg)", "L (20-40 kg)", "XL (>40 kg)")

    val isOwner = editingDog != null && currentUserId > 0 && editingDog.ownerId == currentUserId
    var selectedTab by remember(key) { mutableStateOf(0) }
    var showLostPetDialog by remember { mutableStateOf(false) }
    // Alerta activa reportada en esta misma sesión de la pantalla: el backend no expone un
    // endpoint para consultar "¿tiene esta mascota una alerta activa?", así que este estado no
    // sobrevive a salir de la pantalla o reiniciar la app -- ver nota de alcance en el reporte
    // del Bloque 12.
    var activeAlert by remember(key) { mutableStateOf<AlertaPerdidaDto?>(null) }
    var isMarkingFound by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (editingDog != null) {
                SecondaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Datos") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Expediente Médico") }
                    )
                }
            }

            if (editingDog == null || selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    if (editingDog == null) Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = if (editingDog == null) stringResource(R.string.dog_info_title_new) else stringResource(R.string.dog_info_title_edit),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    OutlinedTextField(
                        value = dogName,
                        onValueChange = { dogName = it },
                        label = { Text("Nombre del perro") },
                        leadingIcon = { Icon(Icons.Outlined.Pets, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Column {
                        OutlinedTextField(
                            value = breed,
                            onValueChange = { breed = it },
                            label = { Text("Raza") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        if (breedMenuExpanded && breedSuggestions.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 4.dp
                            ) {
                                Column {
                                    breedSuggestions.forEach { suggestion ->
                                        Text(
                                            text = suggestion,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    breed = suggestion
                                                    breedMenuExpanded = false
                                                }
                                                .padding(12.dp)
                                        )
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Text("Tamaño", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(18.dp))

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        sizes.forEach { size ->
                            val isSelected = selectedSize == size
                            Surface(
                                modifier = Modifier.border(
                                    width = 2.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .padding(horizontal = 2.dp),
                                shape = RoundedCornerShape(50.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                onClick = { selectedSize = size }
                            ) {
                                Text(
                                    text = size,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (isOwner) {
                        if (activeAlert != null) {
                            Button(
                                onClick = {
                                    val alertId = activeAlert?.id ?: return@Button
                                    if (isMarkingFound) return@Button
                                    isMarkingFound = true
                                    scope.launch {
                                        val response = runCatching {
                                            RetrofitClient.apiService.marcarAlertaEncontrada(alertId, currentUserId)
                                        }.getOrNull()
                                        isMarkingFound = false
                                        if (response?.isSuccessful == true) {
                                            activeAlert = null
                                            snackbarHostState.showSnackbar("¡Qué alegría! Marcamos a $dogName como encontrada.")
                                        } else {
                                            snackbarHostState.showSnackbar("No se pudo actualizar la alerta. Intenta de nuevo.")
                                        }
                                    }
                                },
                                enabled = !isMarkingFound,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(if (isMarkingFound) "Actualizando..." else "¡La encontré!", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showLostPetDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("🚨 Mi mascota se perdió", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            if (isSaving) return@Button

                            if (dogName.isBlank() || breed.isBlank() || selectedSize.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar(emptyFieldsMessage) }
                                return@Button
                            }

                            isSaving = true
                            onFinish(dogName, breed, selectedSize)
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            when {
                                isSaving -> stringResource(R.string.dog_info_saving)
                                editingDog == null -> stringResource(R.string.dog_info_save_new)
                                else -> stringResource(R.string.dog_info_save_edit)
                            }
                        )
                    }
                }
            } else {
                ExpedienteMedicoSection(
                    petId = editingDog.petId,
                    isOwner = isOwner,
                    currentUserId = currentUserId
                )
            }
        }
    }

    if (showLostPetDialog && editingDog != null) {
        ReportarMascotaPerdidaDialog(
            petName = editingDog.name,
            petId = editingDog.petId,
            currentUserId = currentUserId,
            onDismiss = { showLostPetDialog = false },
            onReported = { alert ->
                showLostPetDialog = false
                activeAlert = alert
                scope.launch {
                    snackbarHostState.showSnackbar("Alerta publicada. Notificaremos a usuarios cercanos.")
                }
            }
        )
    }
}
