package com.proyectopoo.petcareapp.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Abre Waze o Google Maps (el usuario elige) con la navegación ya iniciada hacia
 * [latitud]/[longitud]. Si ninguna de las dos apps está instalada, muestra un aviso
 * en vez de fallar silenciosamente o crashear (patrón "fail open" de este codebase).
 */
fun abrirNavegacion(context: Context, latitud: Double, longitud: Double) {
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
        Toast.makeText(context, "No tienes una app de navegación instalada (Waze o Google Maps).", Toast.LENGTH_LONG).show()
    }
}
