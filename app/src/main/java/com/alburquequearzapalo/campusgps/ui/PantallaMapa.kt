package com.alburquequearzapalo.campusgps.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.alburquequearzapalo.campusgps.model.UbicacionUi
import com.alburquequearzapalo.campusgps.util.formatearDistancia
import com.alburquequearzapalo.campusgps.viewmodel.MapaViewModel

private val PERMISOS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsState()
    val lugares by vm.lugaresVisibles.collectAsState()
    val categoria by vm.categoria.collectAsState()
    val seleccionado by vm.seleccionado.collectAsState()
    val masCercano by vm.masCercano.collectAsState()
    val contexto = LocalContext.current

    // El cuadro de permiso lo dibuja Android; aquí solo recibimos la respuesta
    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) vm.buscar() else vm.permisoDenegado()
    }

    fun permisoConcedido() = PERMISOS.any {
        ContextCompat.checkSelfPermission(contexto, it) == PackageManager.PERMISSION_GRANTED
    }

    fun alPulsar() {
        if (permisoConcedido()) vm.buscar() else lanzador.launch(PERMISOS)
    }

    // Se vuelve a revisar cada vez que cambia el estado (por ejemplo, tras responder al permiso)
    val tienePermiso = remember(estado) { permisoConcedido() }

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
        val error = estado as? UbicacionUi.Error
        if (error != null) {
            // Pantalla de error: botón principal, mensaje y la acción que corresponda
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { alPulsar() }) { Text("Obtener mi ubicación") }
                MensajeError(
                    error = error,
                    onAbrirAjustes = { abrirAjustesApp(contexto) },
                    onReintentar = { alPulsar() }
                )
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // El mapa ocupa todo el espacio libre de arriba
            MapaUbicacion(
                estado = estado,
                lugares = lugares,
                seleccionado = seleccionado,
                masCercanoId = masCercano?.id,
                tienePermiso = tienePermiso,
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
                OutlinedButton(
                    onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                    enabled = masCercano != null
                ) {
                    Text("Ir al más cercano")
                }
                masCercano?.let { cercano ->
                    // Solo se formatea un valor ya calculado por el ViewModel
                    val metros = lugares.firstOrNull { it.lugar.id == cercano.id }?.metros
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Más cercano: ") }
                            append("${cercano.nombre} (${metros?.let { formatearDistancia(it) } ?: "Sin calcular"})")
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                when (val e = estado) {
                    UbicacionUi.Inicial -> Text("Pulsa el botón para empezar")
                    UbicacionUi.Buscando -> Text("Buscando…")
                    is UbicacionUi.Lista -> Text(
                        "Latitud: ${e.posicion.latitud}   Longitud: ${e.posicion.longitud}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    is UbicacionUi.Error -> Unit // se dibuja arriba con MensajeError
                }
            }
            // Mejora IA: chips para filtrar por categoría (mapa y lista)
            FiltroCategorias(
                categorias = vm.categorias,
                seleccionada = categoria,
                onElegir = { vm.filtrar(it) }
            )
            // Lista debajo del mapa, con altura fija
            ListaLugares(
                lugares = lugares,
                seleccionado = seleccionado,
                masCercanoId = masCercano?.id,
                onClick = { vm.seleccionar(it) },
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
        }
    }
}

/** Abre la pantalla de información de la app, donde el usuario puede conceder el permiso. */
private fun abrirAjustesApp(contexto: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", contexto.packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    contexto.startActivity(intent)
}
