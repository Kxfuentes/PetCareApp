package com.proyectopoo.petcareapp.navigation

import kotlinx.coroutines.flow.MutableStateFlow

/** Un resultado indexable para la búsqueda global (Parte 3.6): nombre de cuidador/dueño, perro o tipo de servicio. */
data class BusquedaResultItem(
    val title: String,
    val subtitle: String,
    val searchText: String
)

/**
 * Puente para pasar el conjunto de items ya cargados en [OwnerHome]/[CaregiverHome] hacia
 * [BusquedaGlobal] (mismo patrón que [CalendarNavigationBridge]/[CompararOfertasBridge]): la
 * búsqueda es puramente local sobre datos que la pantalla de origen ya tiene en memoria, sin
 * llamadas de red adicionales.
 */
object BusquedaGlobalBridge {
    val items = MutableStateFlow<List<BusquedaResultItem>>(emptyList())

    fun set(value: List<BusquedaResultItem>) {
        items.value = value
    }
}
