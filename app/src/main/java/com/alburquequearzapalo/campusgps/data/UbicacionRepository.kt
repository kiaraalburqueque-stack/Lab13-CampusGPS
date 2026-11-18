package com.alburquequearzapalo.campusgps.data

import com.alburquequearzapalo.campusgps.model.Posicion
import kotlinx.coroutines.flow.Flow

/** Contrato de la fuente de ubicación. El ViewModel solo conoce esta interfaz. */
interface UbicacionRepository {
    suspend fun obtenerPosicion(): Result<Posicion>
    fun playServicesDisponible(): Boolean

    /** Mejora IA: posiciones continuas mientras alguien recolecta el Flow (seguimiento en vivo). */
    fun seguirPosicion(): Flow<Posicion>
}

/** El proveedor no pudo dar una ubicación (normalmente porque el GPS está apagado). */
class UbicacionNoDisponibleException : Exception("No se pudo obtener la ubicación")
