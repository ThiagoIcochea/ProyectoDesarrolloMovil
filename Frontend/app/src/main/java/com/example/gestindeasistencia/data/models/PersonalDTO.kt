package com.example.gestindeasistencia.data.models


data class PersonalDto(
    val idPersonal: Int?,
    val cargo: CargoDto?,
    val documento: DocumentoDto?,
    val nombre: String?,
    val apellPaterno: String?,
    val apellMaterno: String?,
    val nroDocumento: String?,
    val fechaNacimiento: String?,
    val fechaIngreso: String?,
    val email: String?
)