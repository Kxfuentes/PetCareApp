package com.proyectopoo.petcareapp.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Obtiene una única posición GPS actual, a diferencia de [LocationReporter] que reporta en
 * intervalos continuos mientras dura un servicio de Taxi/Paseo. La usa el flujo de "mi mascota
 * se perdió" (Bloque 12) para adjuntar lat/lng a la alerta y a los avistamientos, y la lista de
 * alertas cercanas para saber desde dónde buscar (GET /api/alertas-perdida/cercanas).
 *
 * Devuelve null si no hay permiso de ubicación concedido (fino o aproximado) o si no se pudo
 * obtener una posición; no lanza excepción ni dispara un flujo de solicitud de permiso -- eso
 * queda a cargo de quien llama (mismo alcance mínimo que ya documenta [LocationReporter]).
 */
suspend fun getOneShotLocation(context: Context): Location? {
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) return null

    return runCatching {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()
        suspendCancellableCoroutine<Location?> { continuation ->
            continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                .addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }
    }.getOrNull()
}
