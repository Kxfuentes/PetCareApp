package com.proyectopoo.petcareapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CafeMedio,
    onPrimary = CafeOscuro,

    secondary = CafeClaro,
    onSecondary = CafeOscuro,

    background = FondoClaro,
    onBackground = CafeOscuro,

    surface = FondoCampo,
    onSurface = CafeOscuro
)

private val DarkColorScheme = darkColorScheme(
    primary = CafeClaro,
    onPrimary = CafeOscuro,

    secondary = CafeMedio,
    onSecondary = CafeOscuro
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