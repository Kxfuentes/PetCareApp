package com.proyectopoo.petcareapp.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Abre Waze o Google Maps (el usuario elige) con la navegación ya iniciada hacia
 * [latitud]/[longitud]. Si ninguna de las dos apps está instalada, invoca
 * [onNoNavigationApp] en vez de fallar silenciosamente o crashear (patrón "fail
 * open" de este codebase). Este archivo no depende de UI de Android (Toast/Snackbar):
 * el llamador decide cómo mostrar el aviso (normalmente con el SnackbarHostState de
 * su propia pantalla).
 */
fun abrirNavegacion(
    context: Context,
    latitud: Double,
    longitud: Double,
    onNoNavigationApp: () -> Unit = {}
) {
    val destino = "$latitud,$longitud"
    val intentWaze = Intent(Intent.ACTION_VIEW, Uri.parse("https://waze.com/ul?ll=$destino&navigate=yes")).apply {
        setPackage("com.waze")
    }
    val intentGoogle = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$destino")).apply {
        setPackage("com.google.android.apps.maps")
    }
    val chooserIntent = Intent.createChooser(intentGoogle, "Elige app de navegación").apply {
        putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intentWaze))
    }
    try {
        context.startActivity(chooserIntent)
    } catch (e: ActivityNotFoundException) {
        onNoNavigationApp()
    }
}
