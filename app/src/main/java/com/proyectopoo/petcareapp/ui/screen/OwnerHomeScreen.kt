package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.theme.*

@Composable
fun OwnerHomeScreen(
    onGoToCreate: () -> Unit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Surface(
            color = CafeOscuro,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "🐾 ¡Hola!",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp)
            )
        }

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = FondoClaro
                ),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        CafeClaro,
                        RoundedCornerShape(22.dp)
                    )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Max, Golden Retriever · Tamaño M",
                        color = CafeOscuro,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = { }
                    ) {

                        Text(
                            text = "✏Editar mascotas",
                            color = CafeMedio
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Servicios rápidos",
                color = CafeOscuro,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(300.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                items(services) { service ->

                    Card(
                        onClick = onGoToCreate,
                        colors = CardDefaults.cardColors(
                            containerColor = FondoCrema
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.border(
                            2.dp,
                            CafeClaro,
                            RoundedCornerShape(20.dp)
                        )
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {

                            Icon(
                                imageVector = service.second,
                                contentDescription = service.first,
                                tint = CafeMedio,
                                modifier = Modifier.size(34.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = service.first,
                                color = CafeOscuro,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tus últimas solicitudes",
                color = CafeOscuro,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            ListItem(
                headlineContent = {
                    Text("Paseo para Max - Mañana 4PM")
                },

                leadingContent = {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = CafeMedio
                    )
                }
            )
        }
    }
}