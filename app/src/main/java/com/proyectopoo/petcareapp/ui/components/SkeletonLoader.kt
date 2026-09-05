package com.proyectopoo.petcareapp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Placeholder animado y reutilizable para estados de carga de listas.
 *
 * No depende de ninguna librería externa de "shimmer": la animación de
 * pulso de opacidad se hace únicamente con las APIs nativas de Compose
 * (rememberInfiniteTransition + animateFloat).
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val transition = rememberInfiniteTransition(label = "skeletonTransition")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.12f))
    )
}

/**
 * Placeholder de una fila tipo tarjeta de lista: un avatar circular a la
 * izquierda y 2-3 líneas de texto de distinto ancho a la derecha, imitando
 * la silueta de las tarjetas reales usadas en las pantallas de feed/home.
 */
@Composable
fun SkeletonListItem(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(modifier = Modifier.size(56.dp), shape = CircleShape)
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.55f).height(18.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.35f).height(14.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.25f).height(14.dp))
            }
        }
    }
}

/**
 * Columna de [count] filas [SkeletonListItem], pensada para reemplazar un
 * `CircularProgressIndicator` mientras se carga una lista completa
 * (feeds, secciones de solicitudes/ofertas en las pantallas de inicio).
 */
@Composable
fun SkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 4
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        repeat(count) {
            SkeletonListItem()
        }
    }
}
