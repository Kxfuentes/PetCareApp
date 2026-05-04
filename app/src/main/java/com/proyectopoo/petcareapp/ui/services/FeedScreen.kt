package com.proyectopoo.petcareapp.ui.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Pantalla principal donde el cuidador puede ver
 * las solicitudes de servicio publicadas por los dueños.
 */
@Composable
fun FeedScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Título de la pantalla
        Text(
            text = "Servicios disponibles",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Validación: si no hay servicios, se muestra un mensaje
        if (listaServicios.isEmpty()) {
            Text(
                text = "No hay servicios disponibles por el momento.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {

            // Lista de servicios
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaServicios) { servicio ->
                    ServiceCard(servicio = servicio)
                }
            }
        }
    }
}

/**
 * Tarjeta individual para mostrar la información de cada servicio.
 */
@Composable
fun ServiceCard(servicio: PetService) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = servicio.nombreMascota,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Dueño: ${servicio.nombreDueno}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Servicio: ${servicio.tipoServicio}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Descripción: ${servicio.descripcion}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Ubicación: ${servicio.ubicacion}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Contacto: ${servicio.contacto}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Hora: ${servicio.hora}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {
                        // Aquí se simula aceptar el servicio.
                        // Más adelante podría cambiar el estado del servicio.
                    }
                ) {
                    Text(text = "Aceptar Servicio")
                }

                OutlinedButton(
                    onClick = {
                        // Aquí se simula ver el perfil del dueño.
                        // Más adelante podría navegar a una pantalla de perfil.
                    }
                ) {
                    Text(text = "Ver Perfil")
                }
            }
        }
    }
}