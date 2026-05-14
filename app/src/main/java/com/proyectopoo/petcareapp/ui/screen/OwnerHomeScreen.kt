package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.theme.*

@Composable
fun OwnerHomeScreen(
    onGoToCreate: () -> Unit,
    onEditPets: () -> Unit,
    onGoToFeed: () -> Unit,
    onGoToProfile: () -> Unit
) {
    val services = listOf(
        "Alojamiento" to Icons.Default.NightShelter,
        "Guardería" to Icons.Default.ChildCare,
        "Paseo" to Icons.Default.DirectionsWalk,
        "Taxi" to Icons.Default.LocalTaxi,
        "Peluquería" to Icons.Default.ContentCut,
        "Visitante" to Icons.Default.House
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .verticalScroll(scrollState)
    ) {
        // Header con diseño moderno
        Surface(
            color = CafeOscuro,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
                Text(
                    text = "¡Hola, Bienvenida!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "¿Qué necesita tu mascota hoy?",
                    color = CafeClaro,
                    fontSize = 16.sp
                )
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Card de Mascota optimizada
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(22.dp))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FondoCrema,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(Icons.Default.Pets, null, tint = CafeMedio, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Max", color = CafeOscuro, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Golden Retriever · Tamaño M", color = TextoSuave, fontSize = 14.sp)
                    }
                    IconButton(onClick = onEditPets) {
                        Icon(Icons.Default.Edit, null, tint = CafeMedio)
                    }
                }
            }

            // Acciones Rápidas
            Text(text = "Servicios destacados", color = CafeOscuro, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Grid optimizado manualmente
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                services.chunked(2).forEach { rowServices ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowServices.forEach { service ->
                            Card(
                                onClick = onGoToCreate,
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, CafeClaro.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(service.second, null, tint = CafeMedio, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(service.first, color = CafeOscuro, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Banner Informativo
            Card(
                colors = CardDefaults.cardColors(containerColor = CafeMedio),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¿Buscas paseadores?", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Encuentra a los mejores cerca de ti", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                    Button(
                        onClick = onGoToFeed,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("Explorar", color = CafeMedio, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
