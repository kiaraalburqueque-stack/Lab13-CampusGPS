package com.alburquequearzapalo.campusgps

import com.alburquequearzapalo.campusgps.util.formatearDistancia
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatoTest {

    @Test
    fun menosDeMilMuestraMetrosEnteros() {
        assertEquals("350 m", formatearDistancia(350.7f))
        assertEquals("0 m", formatearDistancia(0f))
        assertEquals("999 m", formatearDistancia(999.9f))
    }

    @Test
    fun desdeMilMuestraKilometrosConUnDecimal() {
        assertEquals("1.0 km", formatearDistancia(1000f))
        assertEquals("1.5 km", formatearDistancia(1500f))
        assertEquals("12.3 km", formatearDistancia(12340f))
    }
}
