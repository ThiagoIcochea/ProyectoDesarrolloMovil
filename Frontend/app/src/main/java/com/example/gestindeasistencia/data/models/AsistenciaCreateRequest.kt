package com.example.gestindeasistencia.data.models


data class AsistenciaCreateRequest(
    val personal: PersonalDto,
    val movimiento: MovimientoDto,
    val fecha: String,
    val ipMarcador: String,  // Formato: "IP | Lat: XX.XXXX, Lng: YY.YYYY"
    val autorizacion: AutorizacionDto?
) {
    companion object {
        /**
         * Crea el string de ipMarcador con la ubicación incluida
         */
        fun formatIpWithLocation(ip: String, latitude: Double, longitude: Double): String {
            return "$ip | Lat: $latitude, Lng: $longitude"
        }
    }
}