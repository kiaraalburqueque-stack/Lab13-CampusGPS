package com.alburquequearzapalo.campusgps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alburquequearzapalo.campusgps.ui.PantallaMapa
import com.alburquequearzapalo.campusgps.ui.theme.CampusGPSTheme
import com.alburquequearzapalo.campusgps.viewmodel.CampusViewModelFactory
import com.alburquequearzapalo.campusgps.viewmodel.MapaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusGPSTheme {
                val vm: MapaViewModel = viewModel(factory = CampusViewModelFactory(applicationContext))
                PantallaMapa(vm)
            }
        }
    }
}
