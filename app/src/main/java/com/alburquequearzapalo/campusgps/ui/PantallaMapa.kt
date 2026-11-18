package com.alburquequearzapalo.campusgps.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alburquequearzapalo.campusgps.model.UbicacionUi
import com.alburquequearzapalo.campusgps.viewmodel.MapaViewModel

private val PERMISOS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsState()
    val lugares by vm.lugares.collectAsState()
    val seleccionado by vm.seleccionado.collectAsState()
    val contexto = LocalContext.current

    // El cuadro de permiso lo dibuja Android; aquí solo recibimos la respuesta
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) vm.buscar() else vm.permisoDenegado()
    }

    fun alPulsar() {
        val tienePermiso = PERMISOS.any {
            ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED
        }
        if (tienePermiso) vm.buscar() else lanzador.launch(PERMISOS)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusGPS", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // El mapa ocupa todo el espacio libre de arriba
            MapaUbicacion(
                estado = estado,
                lugares = lugares,
                seleccionado = seleccionado,
                onSeleccionar = { vm.seleccionar(it) },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(onClick = { alPulsar() }, enabled = estado !is UbicacionUi.Buscando) {
                    Text("Obtener mi ubicación")
                }
                when (val e = estado) {
                    UbicacionUi.Inicial -> Text("Pulsa el botón para empezar")
                    UbicacionUi.Buscando -> Text("Buscando…")
                    is UbicacionUi.Lista -> Text(
                        "Latitud: ${e.posicion.latitud}   Longitud: ${e.posicion.longitud}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    is UbicacionUi.Error -> Text(e.mensaje, color = MaterialTheme.colorScheme.error)
                }
            }
            // Lista debajo del mapa, con altura fija
            ListaLugares(
                lugares = lugares,
                seleccionado = seleccionado,
                onClick = { vm.seleccionar(it) },
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
        }
    }
}
