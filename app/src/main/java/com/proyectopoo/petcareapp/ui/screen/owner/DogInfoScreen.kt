package com.proyectopoo.petcareapp.ui.screen.owner

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.entity.PetEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DogInfoScreen(
    editingDog: PetEntity? = null,
    onFinish: (name: String, breed: String, size: String) -> Unit
) {
    val context = LocalContext.current

    // Clave segura para reiniciar el estado al cambiar de mascota
    val key = editingDog?.petId ?: -1

    var isSaving by remember { mutableStateOf(false) }
    var dogName by remember(key) { mutableStateOf(editingDog?.name.orEmpty()) }
    var breed by remember(key) { mutableStateOf(editingDog?.breed.orEmpty()) }
    var selectedSize by remember(key) { mutableStateOf(editingDog?.size.orEmpty()) }

    val sizes = listOf("XS (1-5 kg)", "S (5-10 kg)", "M (10-20 kg)", "L (20-40 kg)", "XL (>40 kg)")

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = if (editingDog == null) "Cuéntanos sobre tu perro" else "Edita los datos de tu perro",
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

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

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

            Button(
                onClick = {
                    if (isSaving) return@Button

                    if (dogName.isBlank() || breed.isBlank() || selectedSize.isBlank()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
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
                        isSaving -> "Guardando..."
                        editingDog == null -> "Guardar Mascota"
                        else -> "Guardar cambios"
                    }
                )
            }
        }
    }
}