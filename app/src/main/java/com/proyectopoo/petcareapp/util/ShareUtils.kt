package com.proyectopoo.petcareapp.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

/**
 * Comparte un deep link a la solicitud [serviceRequestId] (petcare://solicitud/{id}) vía
 * Intent.ACTION_SEND (texto plano), dejando que el usuario elija con qué app compartirlo.
 *
 * Devuelve `false` si no hay ninguna app que pueda manejar el intent -- caso raro pero posible
 * en algunas configuraciones (ver Intent.resolveActivity) -- para que el llamador muestre un
 * aviso en vez de crashear; `true` si el chooser se lanzó correctamente.
 */
fun compartirSolicitud(context: Context, serviceRequestId: Int): Boolean {
    val link = "petcare://solicitud/$serviceRequestId"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Mira esta solicitud de servicio en PetCare: $link")
    }

    if (shareIntent.resolveActivity(context.packageManager) == null) return false

    return try {
        context.startActivity(Intent.createChooser(shareIntent, "Compartir solicitud"))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
