package com.example.gestindeasistencia.data.models

data class UsuarioDto(
    val idUsuario: Int?,
    val rol: RolDto?,
    val personal: PersonalDto?,
    val usuario: String,
    val password: String?,
    val estado: String?,
    val fechaCreacion: String?
)