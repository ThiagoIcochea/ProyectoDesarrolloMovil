package com.example.gestindeasistencia.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Verifica si se tienen los permisos de ubicación
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Obtiene la última ubicación conocida
     */
    fun getLastLocation(
        onSuccess: (latitude: Double, longitude: Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission()) {
            onError("Permiso de ubicación no concedido")
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        onSuccess(location.latitude, location.longitude)
                    } else {
                        // Si no hay última ubicación, obtener ubicación actual
                        getCurrentLocation(onSuccess, onError)
                    }
                }
                .addOnFailureListener { exception ->
                    onError("Error al obtener ubicación: ${exception.message}")
                }
        } catch (e: SecurityException) {
            onError("Error de seguridad: ${e.message}")
        }
    }

    /**
     * Obtiene la ubicación actual del dispositivo
     */
    private fun getCurrentLocation(
        onSuccess: (latitude: Double, longitude: Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission()) {
            onError("Permiso de ubicación no concedido")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 segundos
        ).apply {
            setMinUpdateIntervalMillis(5000L) // 5 segundos
            setMaxUpdateDelayMillis(15000L) // 15 segundos
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                    // Detener actualizaciones después de obtener la ubicación
                    fusedLocationClient.removeLocationUpdates(this)
                } else {
                    onError("No se pudo obtener la ubicación actual")
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            onError("Error de seguridad: ${e.message}")
        }
    }

    /**
     * Calcula la distancia entre dos puntos GPS en metros
     * Usa la fórmula de Haversine implementada por Android
     * 
     * @param lat1 Latitud del punto 1
     * @param lon1 Longitud del punto 1
     * @param lat2 Latitud del punto 2
     * @param lon2 Longitud del punto 2
     * @return Distancia en metros
     */
    fun calcularDistancia(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    /**
     * Verifica si el usuario está dentro del radio permitido de la empresa
     * 
     * @param latitudActual Latitud actual del usuario
     * @param longitudActual Longitud actual del usuario
     * @return true si está dentro del rango, false si está fuera
     */
    fun estaDentroDeEmpresa(
        latitudActual: Double,
        longitudActual: Double
    ): Boolean {
        // Si el geofencing está deshabilitado, siempre retornar true
        if (!GeoConfig.GEOFENCING_HABILITADO) {
            return true
        }
        
        val distancia = calcularDistancia(
            latitudActual, longitudActual,
            GeoConfig.EMPRESA_LATITUD, GeoConfig.EMPRESA_LONGITUD
        )
        
        return distancia <= GeoConfig.RADIO_PERMITIDO_METROS
    }

    /**
     * Obtiene la distancia actual desde la empresa
     * Útil para mostrar en la UI
     * 
     * @param latitudActual Latitud actual del usuario
     * @param longitudActual Longitud actual del usuario
     * @return Distancia en metros desde la empresa
     */
    fun obtenerDistanciaDesdeEmpresa(
        latitudActual: Double,
        longitudActual: Double
    ): Float {
        return calcularDistancia(
            latitudActual, longitudActual,
            GeoConfig.EMPRESA_LATITUD, GeoConfig.EMPRESA_LONGITUD
        )
    }

    companion object {
        /**
         * Permisos necesarios para la ubicación
         */
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}

