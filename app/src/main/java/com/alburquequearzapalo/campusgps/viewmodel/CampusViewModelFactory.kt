package com.alburquequearzapalo.campusgps.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alburquequearzapalo.campusgps.data.LugaresRepositoryLocal
import com.alburquequearzapalo.campusgps.data.UbicacionRepositoryGoogle

/**
 * Fábrica manual del ViewModel. Aquí (y no en el ViewModel) se usa el Context
 * para construir las implementaciones concretas de los repositorios.
 */
class CampusViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapaViewModel::class.java)) {
            return MapaViewModel(
                UbicacionRepositoryGoogle(context.applicationContext),
                LugaresRepositoryLocal()
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
