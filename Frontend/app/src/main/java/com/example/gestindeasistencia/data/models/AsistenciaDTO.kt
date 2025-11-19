package com.example.gestindeasistencia.data.models

data class AsistenciaDto(
    val idAsistencia: Int?,
    val personal: PersonalDto?,
    val movimiento: MovimientoDto?,
    val fecha: String?,
    val ipMarcador: String?,
    val autorizacion: AutorizacionDto?
)