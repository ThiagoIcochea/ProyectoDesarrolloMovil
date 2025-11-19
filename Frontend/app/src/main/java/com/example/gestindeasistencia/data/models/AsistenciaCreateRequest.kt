package com.example.gestindeasistencia.data.models


data class AsistenciaCreateRequest(
    val personal: PersonalDto,
    val movimiento: MovimientoDto,
    val fecha: String,
    val ipMarcador: String,
    val autorizacion: AutorizacionDto?
)