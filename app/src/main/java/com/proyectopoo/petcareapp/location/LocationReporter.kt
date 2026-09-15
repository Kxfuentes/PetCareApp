package com.proyectopoo.petcareapp.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.proyectopoo.petcareapp.data.network.ApiService
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.data.network.UbicacionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Reporta la posición GPS del cuidador cada [INTERVAL_MS] mientras un servicio de Taxi/Paseo
 * está en curso, vía POST /api/solicitudes/{id}/ubicacion. El dueño la recibe consultando
 * (polling, o vía WebSocket) GET .../ubicacion-actual desde SeguimientoMapaScreen.
 *
 * Alcance deliberadamente mínimo para el permiso de ubicación: si ACCESS_FINE_LOCATION no está
 * concedido, [start] no hace nada (silenciosamente) en vez de lanzar un flujo de solicitud de
 * permiso -- el feature principal (el dueño ve la posición del cuidador) funciona igual cuando
 * el permiso sí está presente, y este codebase no tiene hoy un flujo de permisos de ubicación
 * reutilizable (a diferencia de POST_NOTIFICATIONS en MainActivity, que sí lo tiene). Cubrir
 * cada variante de UX de "permiso denegado" queda fuera del alcance de esta primera versión.
 */
class LocationReporter(
    private val context: Context,
    private val apiService: ApiService = RetrofitClient.apiService
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var callback: LocationCallback? = null

    /** Empieza a reportar la ubicación del dispositivo para [serviceRequestId]. No-op si el
     *  permiso de ubicación fina no está concedido. Reemplaza cualquier reporte en curso. */
    fun start(serviceRequestId: Int, scope: CoroutineScope) {
        stop()

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MS)
            .setMinUpdateIntervalMillis(INTERVAL_MS)
            .build()

        val newCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                scope.launch {
                    runCatching {
                        apiService.postUbicacion(
                            serviceRequestId,
                            UbicacionRequest(latitud = location.latitude, longitud = location.longitude)
                        )
                    }.onFailure { e ->
                        Log.e(TAG, "No se pudo reportar la ubicación", e)
                    }
                }
            }
        }
        callback = newCallback

        try {
            fusedLocationClient.requestLocationUpdates(request, newCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            // El permiso pudo revocarse entre el check de arriba y esta llamada; no-op igual.
            Log.e(TAG, "Permiso de ubicación revocado", e)
            callback = null
        }
    }

    /** Detiene el reporte de ubicación en curso, si lo hay. Seguro de llamar varias veces. */
    fun stop() {
        callback?.let { fusedLocationClient.removeLocationUpdates(it) }
        callback = null
    }

    companion object {
        private const val TAG = "LocationReporter"
        private const val INTERVAL_MS = 5_000L
    }
}
