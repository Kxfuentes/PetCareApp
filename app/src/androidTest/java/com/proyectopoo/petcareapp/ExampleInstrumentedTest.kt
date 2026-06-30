package com.proyectopoo.petcareapp

/*
 * Comentario de modulo PetCare:
 * Archivo del proyecto PetCare. Mantiene una parte especifica de la app y debe conservarse simple de seguir.
 */

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.proyectopoo.petcareapp", appContext.packageName)
    }
}