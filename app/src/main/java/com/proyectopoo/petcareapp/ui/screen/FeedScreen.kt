package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.theme.*
import com.proyectopoo.petcareapp.ui.data.listaServicios

@Composable
fun FeedScreen(onGoToCreate: () -> Unit, onGoToProfile: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(FondoClaro).padding(20.dp)) {
        Text("Servicios Disponibles", style = MaterialTheme.typography.headlineMedium, color = CafeOscuro)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(listaServicios) { servicio ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = FondoCampo),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(servicio.tipoServicio, fontWeight = FontWeight.Bold, color = CafeMedio, fontSize = 18.sp)
                        Text("Mascota: ${servicio.nombreMascota}", color = CafeOscuro)
                        Text(servicio.descripcion, color = TextoSuave)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(servicio.hora, fontWeight = FontWeight.Bold, color = CafeMedio)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onGoToCreate,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CafeMedio)
            ) {
                Text("Publicar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onGoToProfile,
                modifier = Modifier.weight(1f)
            ) {
                Text("Mi Perfil", color = CafeMedio)
            }
        }
    }
}