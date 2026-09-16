package com.proyectopoo.petcareapp.navigation

import com.proyectopoo.petcareapp.data.local.relation.ServiceApplicationDetails
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Puente mínimo para pasar la lista de postulaciones a comparar desde [OwnerHome]/[CaregiverHome]
 * hacia [CompararOfertas] (Parte 3.1). Mismo patrón que [CalendarNavigationBridge]: la ruta
 * `object` solo dispara la navegación, y los datos completos (no serializables de forma trivial
 * en una ruta type-safe) viajan por este objeto en memoria.
 */
object CompararOfertasBridge {
    val ofertas = MutableStateFlow<List<ServiceApplicationDetails>>(emptyList())

    fun set(items: List<ServiceApplicationDetails>) {
        ofertas.value = items
    }

    fun clear() {
        ofertas.value = emptyList()
    }
}
