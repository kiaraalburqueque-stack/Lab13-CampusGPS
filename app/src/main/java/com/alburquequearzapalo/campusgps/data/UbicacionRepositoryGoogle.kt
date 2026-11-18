package com.alburquequearzapalo.campusgps.data

import android.annotation.SuppressLint
import android.content.Context
import com.alburquequearzapalo.campusgps.model.Posicion
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

/**
 * Implementación con FusedLocationProviderClient (Google Play Services Location).
 * Es la única clase que habla con la API de ubicación de Google.
 */
class UbicacionRepositoryGoogle(context: Context) : UbicacionRepository {

    private val appContext = context.applicationContext
    private val cliente = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    @SuppressLint("MissingPermission") // La pantalla verifica el permiso antes de llamar a buscar()
    override suspend fun obtenerPosicion(): Result<Posicion> = runCatching {
        val ubicacion = cliente.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await() ?: throw UbicacionNoDisponibleException() // null = GPS apagado o sin señal
        Posicion(ubicacion.latitude, ubicacion.longitude)
    }

    override fun playServicesDisponible(): Boolean =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(appContext) == ConnectionResult.SUCCESS
}
