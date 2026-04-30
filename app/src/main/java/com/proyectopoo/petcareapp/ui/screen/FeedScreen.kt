package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.proyectopoo.petcareapp.ui.navigation.RoleSection
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.ui.model.sampleServices

@Composable
fun FeedScreen(
    onGoToCreateService: () -> Unit,
    onGoToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Servicios Disponibles",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(sampleServices) { service ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Mascota: ${service.nombreMascota}")
                        Text("Servicio: ${service.TipoServicio}")
                        Text("Descripción: ${service.descripcion}")
                        Text("Ubicación: ${service.ubicacion}")
                        Text("Contacto: ${service.contacto}")
                    }
                }
            }
        }

        Button(
            onClick = onGoToCreateService,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear Solicitud")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onGoToProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver Perfil")
        }
    }
}