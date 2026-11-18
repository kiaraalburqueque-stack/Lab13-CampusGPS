package com.alburquequearzapalo.campusgps.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Fila de chips: "Todos" + una por categoría. Solo avisa la elección; el filtrado lo hace el ViewModel. */
@Composable
fun FiltroCategorias(
    categorias: List<String>,
    seleccionada: String?,
    onElegir: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(selected = seleccionada == null, onClick = { onElegir(null) }, label = { Text("Todos") })
        categorias.forEach { cat ->
            FilterChip(selected = seleccionada == cat, onClick = { onElegir(cat) }, label = { Text(cat) })
        }
    }
}
