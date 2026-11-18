package com.alburquequearzapalo.campusgps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alburquequearzapalo.campusgps.model.TipoError
import com.alburquequearzapalo.campusgps.model.UbicacionUi

/**
 * Recuadro de error y, debajo, el botón que corresponde según el tipo:
 * SIN_PERMISO -> Abrir ajustes; GPS_APAGADO u OTRO -> Reintentar; SIN_PLAY_SERVICES -> sin botón.
 */
@Composable
fun MensajeError(
    error: UbicacionUi.Error,
    onAbrirAjustes: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = error.mensaje,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                .padding(12.dp)
        )
        when (error.tipo) {
            TipoError.SIN_PERMISO -> OutlinedButton(onClick = onAbrirAjustes) { Text("Abrir ajustes") }
            TipoError.GPS_APAGADO, TipoError.OTRO -> OutlinedButton(onClick = onReintentar) { Text("Reintentar") }
            TipoError.SIN_PLAY_SERVICES -> Unit // Reintentar no sirve: falta un componente del sistema
        }
    }
}
