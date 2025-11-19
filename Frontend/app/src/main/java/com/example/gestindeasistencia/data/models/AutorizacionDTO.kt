package com.example.gestindeasistencia.data.models

data class AutorizacionDto(
    val idAutorizacion: Int?,
    val movimiento: MovimientoDto?,
    val usuarioSolicita: UsuarioDto?,
    val usuarioAutoriza: UsuarioDto?,
    val descripcion: String?,
    val fechaSolicitud: String?,
    val fechaAprobacion: String?,
    val estado: String?
)