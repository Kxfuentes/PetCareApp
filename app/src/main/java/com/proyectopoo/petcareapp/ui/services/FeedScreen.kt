package com.proyectopoo.petcareapp.ui.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedScreen() {

    val fondoCrema = Color(0xFFFFF7E8)
    val cafeOscuro = Color(0xFF3B2514)
    val cafeMedio = Color(0xFFB87950)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(fondoCrema, Color(0xFFFFFBF2))
                )
            )
    ) {

        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-70).dp, y = (-70).dp)
                .clip(CircleShape)
                .background(cafeMedio)
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-80).dp)
                .clip(CircleShape)
                .background(cafeOscuro)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(55.dp))

            Text(
                text = "PetCare",
                fontSize = 50.sp,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                color = cafeMedio
            )

            Text(
                text = "Servicios disponibles",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = cafeOscuro
            )

            Spacer(modifier = Modifier.height(22.dp))

            if (listaServicios.isEmpty()) {
                Text(
                    text = "No hay servicios disponibles por el momento.",
                    color = cafeOscuro,
                    fontSize = 15.sp
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 30.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listaServicios) { servicio ->
                        ServiceCard(servicio = servicio)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(servicio: PetService) {

    val cafeOscuro = Color(0xFF3B2514)
    val cafeMedio = Color(0xFFB87950)
    val cafeClaro = Color(0xFFD9A77F)
    val fondoCard = Color(0xFFFFFCF5)
    val textoSuave = Color(0xFF7A5A45)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = fondoCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = servicio.nombreMascota,
                color = cafeOscuro,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Dueño: ${servicio.nombreDueno}",
                color = textoSuave,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoServicio(
                titulo = "Servicio",
                valor = servicio.tipoServicio
            )

            InfoServicio(
                titulo = "Descripción",
                valor = servicio.descripcion
            )

            InfoServicio(
                titulo = "Ubicación",
                valor = servicio.ubicacion
            )

            InfoServicio(
                titulo = "Contacto",
                valor = servicio.contacto
            )

            InfoServicio(
                titulo = "Hora",
                valor = servicio.hora
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Button(
                    onClick = {
                        // Simula aceptar el servicio.
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cafeClaro,
                        contentColor = cafeOscuro
                    )
                ) {
                    Text(
                        text = "Aceptar",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        // Simula ver el perfil del dueño.
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = cafeOscuro
                    )
                ) {
                    Text(
                        text = "Ver Perfil",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InfoServicio(
    titulo: String,
    valor: String
) {
    val cafeOscuro = Color(0xFF3B2514)
    val textoSuave = Color(0xFF7A5A45)

    Column(
        modifier = Modifier.padding(bottom = 7.dp)
    ) {
        Text(
            text = titulo,
            color = cafeOscuro,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = valor,
            color = textoSuave,
            fontSize = 14.sp
        )
    }
}