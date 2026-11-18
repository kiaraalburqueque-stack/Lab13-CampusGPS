package com.alburquequearzapalo.campusgps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alburquequearzapalo.campusgps.data.LugaresRepository
import com.alburquequearzapalo.campusgps.data.UbicacionNoDisponibleException
import com.alburquequearzapalo.campusgps.data.UbicacionRepository
import com.alburquequearzapalo.campusgps.model.Lugar
import com.alburquequearzapalo.campusgps.model.LugarConDistancia
import com.alburquequearzapalo.campusgps.model.Posicion
import com.alburquequearzapalo.campusgps.model.TipoError
import com.alburquequearzapalo.campusgps.model.UbicacionUi
import com.alburquequearzapalo.campusgps.util.distanciaMetros
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepo: UbicacionRepository,
    private val lugaresRepo: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    private val _lugares = MutableStateFlow(
        lugaresRepo.obtenerLugares().map { LugarConDistancia(it, null) }
    )
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    fun buscar() {
        // Sin Play Services no hay FusedLocationProvider: se avisa y no se intenta
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                "Este dispositivo no tiene Google Play Services",
                TipoError.SIN_PLAY_SERVICES
            )
            return
        }
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            ubicacionRepo.obtenerPosicion()
                .onSuccess {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
                .onFailure { _estado.value = errorDe(it) }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
    }

    /** Guarda el lugar tocado (en la lista o en el mapa). null = sin selección. */
    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    /** Se deriva de lugares: el primero con distancia calculada. null si aún no hay ubicación. */
    val masCercano: StateFlow<Lugar?> = lugares
        .map { lista -> lista.firstOrNull { it.metros != null }?.lugar }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Calcula la distancia a cada lugar, ordena de menor a mayor y publica la lista. */
    private fun actualizarDistancias(pos: Posicion) {
        _lugares.value = lugaresRepo.obtenerLugares()
            .map { LugarConDistancia(it, distanciaMetros(pos, it)) }
            .sortedBy { it.metros }
        Log.d("MapaViewModel", "Lugares ordenados: " + _lugares.value.joinToString { "${it.lugar.nombre}=${it.metros}" })
    }

    /** Traduce la excepción del repositorio a un mensaje claro y un tipo de error. */
    private fun errorDe(e: Throwable): UbicacionUi.Error = when (e) {
        is SecurityException -> UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás", TipoError.SIN_PERMISO
        )
        is UbicacionNoDisponibleException -> UbicacionUi.Error(
            "No se pudo obtener la ubicación. Activa el GPS e intenta de nuevo", TipoError.GPS_APAGADO
        )
        else -> UbicacionUi.Error(e.message ?: "No se pudo obtener la ubicación", TipoError.OTRO)
    }
}
