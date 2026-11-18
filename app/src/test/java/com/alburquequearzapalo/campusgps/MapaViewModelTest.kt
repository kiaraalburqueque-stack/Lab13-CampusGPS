package com.alburquequearzapalo.campusgps

import com.alburquequearzapalo.campusgps.data.LugaresRepositoryLocal
import com.alburquequearzapalo.campusgps.data.UbicacionRepository
import com.alburquequearzapalo.campusgps.model.Posicion
import com.alburquequearzapalo.campusgps.model.TipoError
import com.alburquequearzapalo.campusgps.model.UbicacionUi
import com.alburquequearzapalo.campusgps.viewmodel.MapaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Repositorio falso: permite probar el ViewModel sin GPS ni Google Play Services. */
private class UbicacionFalsa(private val hayPlayServices: Boolean = true) : UbicacionRepository {
    override suspend fun obtenerPosicion() = Result.success(Posicion(-12.0464, -77.0428))
    override fun playServicesDisponible() = hayPlayServices
}

@OptIn(ExperimentalCoroutinesApi::class)
class MapaViewModelTest {

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(playServices: Boolean = true) =
        MapaViewModel(UbicacionFalsa(playServices), LugaresRepositoryLocal())

    @Test
    fun categoriasSonLasCuatroDelLaboratorio() {
        assertEquals(listOf("Estudio", "Comida", "Eventos", "Servicios"), vm().categorias)
    }

    @Test
    fun filtrarPorEstudioDejaSoloDosLugares() {
        val vm = vm()
        vm.filtrar("Estudio")
        val visibles = vm.lugaresVisibles.value
        assertEquals(2, visibles.size)
        assertTrue(visibles.all { it.lugar.categoria == "Estudio" })
    }

    @Test
    fun quitarFiltroMuestraLosCinco() {
        val vm = vm()
        vm.filtrar("Comida")
        vm.filtrar(null)
        assertEquals(5, vm.lugaresVisibles.value.size)
    }

    @Test
    fun filtrarQuitaSeleccionSiQuedaOculta() {
        val vm = vm()
        vm.seleccionar(2) // Cafetería Campus (Comida)
        vm.filtrar("Eventos")
        assertNull(vm.seleccionado.value)
    }

    @Test
    fun sinPlayServicesMuestraErrorSinPlayServices() {
        val vm = vm(playServices = false)
        vm.buscar()
        assertEquals(TipoError.SIN_PLAY_SERVICES, (vm.estado.value as UbicacionUi.Error).tipo)
    }

    @Test
    fun permisoDenegadoEsErrorSinPermiso() {
        val vm = vm()
        vm.permisoDenegado()
        assertEquals(TipoError.SIN_PERMISO, (vm.estado.value as UbicacionUi.Error).tipo)
    }
}
