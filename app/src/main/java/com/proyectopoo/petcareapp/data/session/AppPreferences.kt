package com.proyectopoo.petcareapp.data.session

import android.content.Context

/**
 * Preferencias de la app que no son de sesión (Parte 3.9 - Configuración): por ahora, solo el
 * override manual de modo oscuro. Mismo patrón de SharedPreferences que [SessionManager].
 */
class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("petcare_prefs", Context.MODE_PRIVATE)

    /** null = seguir el tema del sistema; true/false = override manual del usuario. */
    fun getDarkModeOverride(): Boolean? =
        if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null

    fun setDarkModeOverride(value: Boolean?) {
        val editor = prefs.edit()
        if (value == null) editor.remove(KEY_DARK_MODE) else editor.putBoolean(KEY_DARK_MODE, value)
        editor.apply()
    }

    companion object {
        private const val KEY_DARK_MODE = "dark_mode_override"
    }
}
