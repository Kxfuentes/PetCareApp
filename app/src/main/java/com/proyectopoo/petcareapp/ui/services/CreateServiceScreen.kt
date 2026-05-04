package com.proyectopoo.petcareapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Pantalla para que el dueño cree una nueva solicitud de servicio.
 */
@Composable
fun CreateServiceScreen() {

    var nombreMascota by remember { mutableStateOf("") }
    var nombreDueno by remember { mutableStateOf("") }
    var tipoServicio by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var contacto by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    var mensajeError by remember { mutableStateOf("") }
    var mensajeExito by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "Crear solicitud de servicio",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = nombreMascota,
            onValueChange = { nombreMascota = it },
            label = { Text("Nombre de la mascota") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nombreDueno,
            onValueChange = { nombreDueno = it },
            label = { Text("Nombre del dueño") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tipoServicio,
            onValueChange = { tipoServicio = it },
            label = { Text("Tipo de servicio") },
            placeholder = { Text("Ej: Paseo, veterinario, cuidado") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Ubicación") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contacto,
            onValueChange = { contacto = it },
            label = { Text("Contacto") },
            placeholder = { Text("Ej: 8888-8888") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = hora,
            onValueChange = { hora = it },
            label = { Text("Hora") },
            placeholder = { Text("Ej: 10:00 AM") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                mensajeError = ""
                mensajeExito = ""

                // Validación de campos vacíos
                if (
                    nombreMascota.isBlank() ||
                    nombreDueno.isBlank() ||
                    tipoServicio.isBlank() ||
                    descripcion.isBlank() ||
                    ubicacion.isBlank() ||
                    contacto.isBlank() ||
                    hora.isBlank()
                ) {
                    mensajeError = "Por favor complete todos los campos."
                    return@Button
                }

                // Validación básica del contacto
                if (contacto.length < 8) {
                    mensajeError = "El contacto debe tener al menos 8 caracteres."
                    return@Button
                }

                // Creación del nuevo servicio
                val nuevoServicio = PetService(
                    id = listaServicios.size + 1,
                    nombreMascota = nombreMascota.trim(),
                    nombreDueno = nombreDueno.trim(),
                    tipoServicio = tipoServicio.trim(),
                    descripcion = descripcion.trim(),
                    ubicacion = ubicacion.trim(),
                    contacto = contacto.trim(),
                    hora = hora.trim()
                )

                // Se agrega el servicio a la lista temporal
                listaServicios.add(nuevoServicio)

                mensajeExito = "Solicitud publicada correctamente."

                // Limpiar campos después de publicar
                nombreMascota = ""
                nombreDueno = ""
                tipoServicio = ""
                descripcion = ""
                ubicacion = ""
                contacto = ""
                hora = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Publicar")
        }

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (mensajeExito.isNotEmpty()) {
            Text(
                text = mensajeExito,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}