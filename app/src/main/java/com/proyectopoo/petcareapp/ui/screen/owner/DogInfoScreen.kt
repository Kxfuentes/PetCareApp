package com.proyectopoo.petcareapp.ui.screen.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.entity.PetEntity

@Composable
fun DogInfoScreen(
    initialDog: PetEntity? = null,
    onFinish: (PetEntity) -> Unit
) {

    var name by remember { mutableStateOf(initialDog?.name ?: "") }
    var breed by remember { mutableStateOf(initialDog?.breed ?: "") }
    var size by remember { mutableStateOf(initialDog?.size ?: "") }

    val sizes = listOf("XS", "S", "M", "L", "XL")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {

            Text(
                text = if (initialDog == null) "Agregar mascota" else "Editar mascota",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it },
                label = { Text("Raza") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Text("Tamaño")

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sizes.forEach { s ->
                    FilterChip(
                        selected = size == s,
                        onClick = { size = s },
                        label = { Text(s) }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    onFinish(
                        PetEntity(
                            petId = initialDog?.petId ?: System.currentTimeMillis().toInt(),
                            ownerId = 1,
                            name = name,
                            breed = breed,
                            size = size,
                            species = "Dog"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (initialDog == null) "Crear mascota" else "Guardar cambios")
            }
        }
    }
}