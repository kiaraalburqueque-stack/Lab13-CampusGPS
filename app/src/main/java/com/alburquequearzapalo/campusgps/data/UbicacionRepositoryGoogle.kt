package com.alburquequearzapalo.campusgps.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.alburquequearzapalo.campusgps.model.Posicion
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    /**
     * Convierte el callback de requestLocationUpdates en un Flow (callbackFlow).
     * Cuando el ViewModel deja de recolectar, awaitClose quita las actualizaciones
     * para no gastar batería.
     */
    @SuppressLint("MissingPermission") // La pantalla verifica el permiso antes de iniciar
    override fun seguirPosicion(): Flow<Posicion> = callbackFlow {
        val pedido = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3_000L)
            .setMinUpdateDistanceMeters(3f)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(resultado: LocationResult) {
                resultado.lastLocation?.let { trySend(Posicion(it.latitude, it.longitude)) }
            }
        }
        cliente.requestLocationUpdates(pedido, callback, Looper.getMainLooper())
            .addOnFailureListener { close(it) }
        awaitClose { cliente.removeLocationUpdates(callback) }
    }

    override fun playServicesDisponible(): Boolean =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(appContext) == ConnectionResult.SUCCESS
}
