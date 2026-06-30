package com.proyectopoo.petcareapp.ui.components

/*
 * Comentario de modulo PetCare:
 * Componente reutilizable de interfaz. Se mantiene aislado para repetirlo sin duplicar codigo.
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun StarRatingInput(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Trabajamos con medios puntos para permitir calificaciones como 4.5.
    val roundedValue = value.roundToHalf()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { index ->
                val isFull = roundedValue >= index
                val isHalf = !isFull && roundedValue >= index - 0.5f
                Icon(
                    imageVector = if (isFull || isHalf) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = when {
                        isFull -> Color(0xFFFFB300)
                        isHalf -> Color(0xFFFFB300).copy(alpha = 0.55f)
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
            }
        }

        Text(
            text = "${roundedValue.formatRating()} estrellas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Slider(
            value = roundedValue,
            onValueChange = { onValueChange(it.roundToHalf()) },
            valueRange = 1f..5f,
            steps = 7
        )
    }
}

private fun Float.roundToHalf(): Float {
    // Se limita entre 1 y 5 para evitar valores invalidos al guardar.
    return ((this.coerceIn(1f, 5f) * 2f).roundToInt() / 2f)
}

private fun Float.formatRating(): String {
    return if (this % 1f == 0f) this.toInt().toString() else String.format("%.1f", this)
}
