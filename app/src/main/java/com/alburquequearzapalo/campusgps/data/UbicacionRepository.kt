package com.alburquequearzapalo.campusgps.data

import com.alburquequearzapalo.campusgps.model.Posicion

/** Contrato de la fuente de ubicación. El ViewModel solo conoce esta interfaz. */
interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
}
