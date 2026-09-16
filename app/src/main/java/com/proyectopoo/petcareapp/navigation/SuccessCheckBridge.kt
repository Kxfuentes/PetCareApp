package com.proyectopoo.petcareapp.navigation

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Señaliza que la pantalla de destino debe mostrar [com.proyectopoo.petcareapp.ui.components.SuccessCheckOverlay]
 * apenas se componga (Parte 3.12), para acciones que navegan a otra pantalla antes de poder
 * mostrar la animación (p. ej. crear una solicitud). Mismo patrón que [CalendarNavigationBridge].
 */
object SuccessCheckBridge {
    val pending = MutableStateFlow(false)

    fun trigger() {
        pending.value = true
    }

    fun consume(): Boolean {
        val value = pending.value
        pending.value = false
        return value
    }
}
