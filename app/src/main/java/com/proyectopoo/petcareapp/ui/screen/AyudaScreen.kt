package com.proyectopoo.petcareapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class FaqItem(val pregunta: String, val respuesta: String)

private val faq = listOf(
    FaqItem("¿Cómo cambio de rol si me equivoqué?", "No se puede cambiar de rol una vez confirmado — contacta a soporte."),
    FaqItem("¿Puedo registrar un gato u otra mascota?", "No, PetCare es exclusivamente para perros."),
    FaqItem("¿Por qué no veo el botón de registrar mascota?", "Los cuidadores no pueden registrar mascotas propias — si necesitas registrar una, tu cuenta debe ser de propietario."),
    FaqItem("¿Puedo vender o regalar un perro en la plataforma?", "No, está explícitamente prohibido y el sistema rechaza automáticamente publicaciones con ese tipo de lenguaje."),
    FaqItem("¿Cómo funciona el sistema de badges?", "Es una etiqueta calculada automáticamente para cada cuidador según servicios completados, calificación promedio y cancelaciones."),
    FaqItem("¿Qué pasa si el cuidador no se presenta?", "Cancela la solicitud desde la app y, si corresponde, refléjalo en la calificación o repórtalo como emergencia si el servicio ya había iniciado."),
    FaqItem("¿Cómo se manejan las cancelaciones?", "Una solicitud o postulación se puede cancelar mientras no esté completada, guardando opcionalmente un motivo."),
    FaqItem("¿Los datos están seguros?", "Las contraseñas se guardan hasheadas (nunca en texto plano) y la sesión se maneja con un token que expira a los 7 días."),
    FaqItem("El mapa se ve con una marca de agua", "Es el comportamiento esperado sin una API Key real de Google Maps configurada.")
)

private data class Guia(val titulo: String, val pasos: List<String>)

private val guias = listOf(
    Guia("Cómo crear una solicitud de servicio", listOf(
        "Elige tu perro y el tipo de servicio (paseo, guardería, taxi, peluquería, visitante o alojamiento).",
        "Indica fecha, hora y ubicación.",
        "Espera postulaciones de cuidadores, o busca directamente entre las ofertas publicadas."
    )),
    Guia("Cómo ofertar a una solicitud (cuidadores)", listOf(
        "Desde el feed de solicitudes disponibles, revisa los detalles y el expediente médico del perro.",
        "Presiona \"Ofrecer mis servicios\".",
        "Espera a que el dueño acepte tu postulación para empezar a coordinar por chat."
    )),
    Guia("Cómo completar un servicio", listOf(
        "Marca el servicio como iniciado y sube una foto de evidencia (ANTES).",
        "Al terminar, sube la foto de evidencia (DESPUÉS).",
        "El dueño confirma la finalización y puede calificarte."
    ))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyudaScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayuda", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Guías rápidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            guias.forEach { guia ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(guia.titulo, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        guia.pasos.forEachIndexed { index, paso ->
                            Text("${index + 1}. $paso", style = MaterialTheme.typography.bodySmall)
                            if (index != guia.pasos.lastIndex) Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }

            Text("Preguntas frecuentes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            faq.forEach { item ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        Modifier
                            .clickable { expanded = !expanded }
                            .padding(16.dp)
                    ) {
                        Text(item.pregunta, fontWeight = FontWeight.SemiBold)
                        if (expanded) {
                            Spacer(Modifier.height(6.dp))
                            Text(item.respuesta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Kxfuentes/PetCareApp/issues"))
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Contactar soporte / reportar un bug", fontWeight = FontWeight.Bold)
            }
        }
    }
}
