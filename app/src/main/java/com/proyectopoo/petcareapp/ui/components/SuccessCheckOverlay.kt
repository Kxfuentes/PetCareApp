package com.proyectopoo.petcareapp.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Animación de check verde (Parte 3.12) que se muestra brevemente tras completar una acción
 * clave (crear solicitud, aceptar oferta, completar servicio, enviar calificación) y luego
 * llama [onFinished] para que el llamador la oculte.
 */
@Composable
fun SuccessCheckOverlay(onFinished: () -> Unit) {
    val scale = remember { mutableFloatStateOf(0f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale.floatValue,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "successCheckScale"
    )

    LaunchedEffect(Unit) {
        scale.floatValue = 1f
        delay(900)
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF2E7D32),
            modifier = Modifier
                .size(88.dp)
                .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }
    }
}
