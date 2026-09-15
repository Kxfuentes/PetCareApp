package com.proyectopoo.petcareapp.util

import com.proyectopoo.petcareapp.ui.screen.FiltrosResult
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltrosUtilsTest {

    @Test
    fun `null filters always match`() {
        assertTrue(matchesFiltros(null, "Paseo", 2.0, 500.0))
        assertTrue(matchesFiltros(null, null, null, null))
    }

    @Test
    fun `service type mismatch excludes`() {
        val filters = FiltrosResult(radiusKm = 15f, serviceType = "Paseo", minRating = 0f)
        assertFalse(matchesFiltros(filters, "Guardería", rating = null, distanceKm = null))
    }

    @Test
    fun `service type match (case-insensitive) is kept`() {
        val filters = FiltrosResult(radiusKm = 15f, serviceType = "Paseo", minRating = 0f)
        assertTrue(matchesFiltros(filters, "paseo", rating = null, distanceKm = null))
    }

    @Test
    fun `rating below minRating excludes`() {
        val filters = FiltrosResult(radiusKm = 15f, serviceType = null, minRating = 4f)
        assertFalse(matchesFiltros(filters, null, rating = 3.5, distanceKm = null))
    }

    @Test
    fun `rating at or above minRating is kept`() {
        val filters = FiltrosResult(radiusKm = 15f, serviceType = null, minRating = 4f)
        assertTrue(matchesFiltros(filters, null, rating = 4.5, distanceKm = null))
    }

    @Test
    fun `distance beyond radiusKm excludes`() {
        val filters = FiltrosResult(radiusKm = 10f, serviceType = null, minRating = 0f)
        assertFalse(matchesFiltros(filters, null, rating = null, distanceKm = 25.0))
    }

    @Test
    fun `distance within radiusKm is kept`() {
        val filters = FiltrosResult(radiusKm = 10f, serviceType = null, minRating = 0f)
        assertTrue(matchesFiltros(filters, null, rating = null, distanceKm = 5.0))
    }

    @Test
    fun `null rating and distance dont exclude (fail open)`() {
        val filters = FiltrosResult(radiusKm = 10f, serviceType = null, minRating = 4f)
        assertTrue(matchesFiltros(filters, null, rating = null, distanceKm = null))
    }
}
