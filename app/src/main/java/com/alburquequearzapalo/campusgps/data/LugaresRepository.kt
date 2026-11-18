package com.alburquequearzapalo.campusgps.data

import com.alburquequearzapalo.campusgps.model.Lugar

/** Contrato de la fuente de lugares del campus. */
interface LugaresRepository {
    fun obtenerLugares(): List<Lugar>
}
