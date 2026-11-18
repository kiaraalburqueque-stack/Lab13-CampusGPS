package com.alburquequearzapalo.campusgps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.ui.Modifier
import com.alburquequearzapalo.campusgps.model.LugarConDistancia
import com.alburquequearzapalo.campusgps.model.UbicacionUi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

/** Solo dibuja lo que recibe: no consulta repositorios ni calcula nada. */
@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    masCercanoId: Int?,
    modifier: Modifier = Modifier
) {
    val camara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-12.0464, -77.0428), 12f)
    }

    // Cuando llega la ubicación, la cámara viaja hasta ella
    LaunchedEffect(estado) {
        if (estado is UbicacionUi.Lista) {
            val p = estado.posicion
            camara.animate(CameraUpdateFactory.newLatLngZoom(LatLng(p.latitud, p.longitud), 16f))
        }
    }

    // Al seleccionar un lugar, la cámara viaja hasta él
    LaunchedEffect(seleccionado) {
        lugares.firstOrNull { it.lugar.id == seleccionado }?.let {
            camara.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.lugar.latitud, it.lugar.longitud), 17f))
        }
    }

    GoogleMap(modifier = modifier, cameraPositionState = camara) {
        if (estado is UbicacionUi.Lista) {
            Marker(
                state = rememberUpdatedMarkerState(
                    position = LatLng(estado.posicion.latitud, estado.posicion.longitud)
                ),
                title = "Estás aquí"
            )
        }
        // Un marcador por lugar; key(id) evita que Compose confunda marcadores al reordenar
        lugares.forEach { item ->
            key(item.lugar.id) {
                val estadoMarcador = rememberUpdatedMarkerState(
                    position = LatLng(item.lugar.latitud, item.lugar.longitud)
                )
                // El lugar seleccionado muestra su nombre y categoría
                LaunchedEffect(seleccionado) {
                    if (seleccionado == item.lugar.id) estadoMarcador.showInfoWindow()
                }
                Marker(
                    state = estadoMarcador,
                    title = item.lugar.nombre,
                    snippet = item.lugar.categoria,
                    // El más cercano en verde; los demás en azul
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (item.lugar.id == masCercanoId) BitmapDescriptorFactory.HUE_GREEN
                        else BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }
        }
    }
}
