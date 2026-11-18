package com.alburquequearzapalo.campusgps.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Azul del tema: es el fondo de la TopAppBar y de los botones principales
private val Azul = Color(0xFF1B6CA8)
private val AzulClaro = Color(0xFF9CCBFF)

private val ColoresClaros = lightColorScheme(
    primary = Azul,
    onPrimary = Color.White,
    secondary = Color(0xFF2E7D32)
)

private val ColoresOscuros = darkColorScheme(
    primary = AzulClaro,
    onPrimary = Color(0xFF00325A),
    secondary = Color(0xFF81C784)
)

@Composable
fun CampusGPSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ColoresOscuros else ColoresClaros,
        content = content
    )
}
