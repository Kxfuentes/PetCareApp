package com.proyectopoo.petcareapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CafeMedio,
    onPrimary = Blanco,

    secondary = CafeClaro,
    onSecondary = CafeOscuro,

    background = FondoClaro,
    onBackground = CafeOscuro,

    surface = FondoCampo,
    onSurface = CafeOscuro,
    surfaceVariant = FondoCrema,
    onSurfaceVariant = TextoSuave,

    outline = BordeCampo,
    outlineVariant = BordeCampo.copy(alpha = 0.5f),

    error = ErrorRed,
    onError = Blanco,

    primaryContainer = CafeClaro,
    onPrimaryContainer = CafeOscuro,

    secondaryContainer = FondoCrema,
    onSecondaryContainer = CafeOscuro
)

private val DarkColorScheme = darkColorScheme(
    primary = CafeClaro,
    onPrimary = CafeOscuro,

    secondary = CafeMedio,
    onSecondary = Blanco,

    background = Color(0xFF2C2119),
    onBackground = Color(0xFFEAE0D5),

    surface = Color(0xFF3B2E24),
    onSurface = Color(0xFFEAE0D5),
    surfaceVariant = Color(0xFF4A3A2F),
    onSurfaceVariant = Color(0xFFD4C3B5),

    outline = Color(0xFF8A6A55),
    error = ErrorRed,
    onError = Blanco
)

@Composable
fun PetCareAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}