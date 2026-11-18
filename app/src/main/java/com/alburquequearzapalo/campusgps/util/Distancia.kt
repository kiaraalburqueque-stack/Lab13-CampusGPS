package com.alburquequearzapalo.campusgps.util

import android.location.Location
import com.alburquequearzapalo.campusgps.model.Lugar
import com.alburquequearzapalo.campusgps.model.Posicion

/**
 * Distancia en metros entre tu posición y un lugar.
 * Location.distanceBetween guarda el resultado en resultado[0].
 * Ojo con el orden: (lat1, lon1, lat2, lon2, resultado).
 */
fun distanciaMetros(desde: Posicion, hasta: Lugar): Float {
    val resultado = FloatArray(1)
    Location.distanceBetween(
        desde.latitud, desde.longitud,
        hasta.latitud, hasta.longitud,
        resultado
    )
    return resultado[0]
}
