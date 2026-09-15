package com.proyectopoo.petcareapp.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceUtilsTest {

    @Test
    fun `haversineKm returns ~0 for the same point`() {
        val km = DistanceUtils.haversineKm(12.1364, -86.2514, 12.1364, -86.2514)
        assertEquals(0.0, km, 0.0001)
    }

    @Test
    fun `haversineKm returns a known real-world distance`() {
        // Managua, Nicaragua to Leon, Nicaragua: roughly 75-76 km in a straight line.
        val managuaLat = 12.1364
        val managuaLon = -86.2514
        val leonLat = 12.4344
        val leonLon = -86.8780

        val km = DistanceUtils.haversineKm(managuaLat, managuaLon, leonLat, leonLon)

        assertEquals(75.7, km, 5.0)
    }

    @Test
    fun `formatKm formats with one decimal and km suffix`() {
        assertEquals("12.3 km", DistanceUtils.formatKm(12.34))
    }
}
