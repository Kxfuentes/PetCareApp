package com.proyectopoo.petcareapp.navigation

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Puente mínimo para pasar "abrir el detalle de esta solicitud" desde [Calendario] de vuelta a
 * [OwnerHome]/[CaregiverHome] al volver atrás (Bloque 9).
 *
 * `OwnerHome`/`CaregiverHome` son rutas `object` referenciadas sin paréntesis en varios puntos
 * de `AppNavigation.kt` y en `MainActivity.kt` (código local del usuario, fuera de este cambio);
 * convertirlas en `data class` con un parámetro `openServiceRequestId` para pasar este dato por
 * la ruta misma habría obligado a tocar ese archivo. En su lugar, este objeto guarda el id
 * pendiente y las pantallas de inicio lo consumen vía un parámetro normal
 * (`initialFocusServiceRequestId`) y lo limpian con [clear] una vez que abren el diálogo de
 * detalle correspondiente.
 */
object CalendarNavigationBridge {
    val pendingServiceRequestId = MutableStateFlow<Int?>(null)

    fun request(serviceRequestId: Int) {
        pendingServiceRequestId.value = serviceRequestId
    }

    fun clear() {
        pendingServiceRequestId.value = null
    }
}
