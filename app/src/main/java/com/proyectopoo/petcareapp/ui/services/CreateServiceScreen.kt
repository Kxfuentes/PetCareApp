package com.proyectopoo.petcareapp.ui.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    val fondoCrema = Color(0xFFFFF7E8)
    val cafeOscuro = Color(0xFF3B2514)
    val cafeMedio = Color(0xFFB87950)
    val cafeClaro = Color(0xFFD9A77F)

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
                .offset(x = (-60).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(cafeMedio)
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-70).dp)
                .clip(CircleShape)
                .background(cafeOscuro)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "PetCare",
                fontSize = 50.sp,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                color = cafeMedio
            )

            Text(
                text = "Crear Solicitud",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = cafeOscuro
            )

            Spacer(modifier = Modifier.height(22.dp))

            CampoPetCare("Nombre mascota", nombreMascota, "Ej. Bandi") { nombreMascota = it }
            CampoPetCare("Nombre dueño", nombreDueno, "Ej. Nahúm Espinoza") { nombreDueno = it }
            CampoPetCare("Tipo servicio", tipoServicio, "Ej. Paseo, baño, cuidado") { tipoServicio = it }
            CampoPetCare("Descripción", descripcion, "Describe el servicio") { descripcion = it }
            CampoPetCare("Ubicación", ubicacion, "Ej. Ciudad, barrio o dirección") { ubicacion = it }
            CampoPetCare("Contacto", contacto, "Ej. 8215-0193") { contacto = it }
            CampoPetCare("Hora", hora, "Ej. 3:00 PM") { hora = it }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    mensajeError = ""
                    mensajeExito = ""

                    if (
                        nombreMascota.isBlank() ||
                        nombreDueno.isBlank() ||
                        tipoServicio.isBlank() ||
                        descripcion.isBlank() ||
                        ubicacion.isBlank() ||
                        contacto.isBlank() ||
                        hora.isBlank()
                    ) {
                        mensajeError = "Complete todos los campos."
                        return@Button
                    }

                    if (contacto.length < 8) {
                        mensajeError = "Ingrese un contacto válido."
                        return@Button
                    }

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

                    listaServicios.add(nuevoServicio)

                    mensajeExito = "Solicitud publicada correctamente."

                    nombreMascota = ""
                    nombreDueno = ""
                    tipoServicio = ""
                    descripcion = ""
                    ubicacion = ""
                    contacto = ""
                    hora = ""
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = cafeClaro,
                    contentColor = cafeOscuro
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(52.dp)
            ) {
                Text(
                    text = "Publicar Solicitud",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (mensajeError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (mensajeExito.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = mensajeExito,
                    color = cafeOscuro,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CampoPetCare(
    label: String,
    valor: String,
    placeholder: String,
    onChange: (String) -> Unit
) {
    val cafeOscuro = Color(0xFF3B2514)
    val cafeMedio = Color(0xFFB87950)
    val fondoCampo = Color(0xFFFFFCF5)
    val bordeCampo = Color(0xFFD6A57A)
    val textoPlaceholder = Color(0xFF8A6A55)

    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = label,
            color = cafeOscuro,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = textoPlaceholder
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = cafeOscuro,
                unfocusedTextColor = cafeOscuro,
                cursorColor = cafeOscuro,
                focusedContainerColor = fondoCampo,
                unfocusedContainerColor = fondoCampo,
                focusedBorderColor = cafeMedio,
                unfocusedBorderColor = bordeCampo
            )
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}