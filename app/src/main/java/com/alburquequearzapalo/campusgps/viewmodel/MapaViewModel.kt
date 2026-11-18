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
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    // Mejora IA: seguimiento en vivo. El Job vive en viewModelScope, así sobrevive a la rotación
    private val _siguiendo = MutableStateFlow(false)
    val siguiendo: StateFlow<Boolean> = _siguiendo.asStateFlow()
    private var trabajoSeguimiento: Job? = null

    fun iniciarSeguimiento() {
        if (_siguiendo.value) return
        if (!ubicacionRepo.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                "Este dispositivo no tiene Google Play Services",
                TipoError.SIN_PLAY_SERVICES
            )
            return
        }
        _siguiendo.value = true
        if (_estado.value !is UbicacionUi.Lista) _estado.value = UbicacionUi.Buscando
        trabajoSeguimiento = viewModelScope.launch {
            ubicacionRepo.seguirPosicion()
                .catch { _estado.value = errorDe(it) }
                .onCompletion {
                    // Solo apaga el indicador si este es el seguimiento vigente (evita carreras al reiniciar)
                    val actual = currentCoroutineContext()[Job]
                    if (trabajoSeguimiento == null || trabajoSeguimiento == actual) _siguiendo.value = false
                }
                .collect {
                    _estado.value = UbicacionUi.Lista(it)
                    actualizarDistancias(it)
                }
        }
    }

    fun detenerSeguimiento() {
        trabajoSeguimiento?.cancel()
        trabajoSeguimiento = null
        _siguiendo.value = false
    }

    fun permisoDenegado() {
        detenerSeguimiento()
        _estado.value = UbicacionUi.Error(
            "Sin permiso de ubicación no puedo mostrar dónde estás",
            TipoError.SIN_PERMISO
        )
    }

    fun filtrar(categoria: String?) {
        _categoria.value = categoria
        // Si el lugar seleccionado queda oculto por el filtro, se quita la selección
        val visibles = filtrarLista(_lugares.value, categoria)
        if (visibles.none { it.lugar.id == _seleccionado.value }) _seleccionado.value = null
    }

    private fun filtrarLista(lista: List<LugarConDistancia>, cat: String?) =
        if (cat == null) lista else lista.filter { it.lugar.categoria == cat }

    /** Guarda el lugar tocado (en la lista o en el mapa). null = sin selección. */
    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    // Mejora IA: filtro por categoría. null = todas las categorías
    private val _categoria = MutableStateFlow<String?>(null)
    val categoria: StateFlow<String?> = _categoria.asStateFlow()

    /** Categorías disponibles, en el orden en que aparecen en el repositorio. */
    val categorias: List<String> = lugaresRepo.obtenerLugares().map { it.categoria }.distinct()

    /** Lo que ven el mapa y la lista: lugares (ya ordenados) filtrados por la categoría elegida. */
    val lugaresVisibles: StateFlow<List<LugarConDistancia>> =
        combine(_lugares, _categoria) { lista, cat -> filtrarLista(lista, cat) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, _lugares.value)

    /** Se deriva de los lugares visibles: el primero con distancia calculada. null si aún no hay ubicación. */
    val masCercano: StateFlow<Lugar?> = lugaresVisibles
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
