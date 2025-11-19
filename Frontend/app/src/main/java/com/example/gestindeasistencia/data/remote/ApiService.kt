package com.example.gestindeasistencia.data.remote

import com.example.gestindeasistencia.data.models.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- AUTH ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<String>

    @GET("auth/validar")
    suspend fun validarToken(): Response<String>

    // --- USUARIO ---
    @GET("usuario")
    suspend fun listarUsuarios(): Response<List<UsuarioDto>>

    @GET("usuario/{id}")
    suspend fun obtenerUsuario(@Path("id") id: Int): Response<UsuarioDto>

    @POST("usuario")
    suspend fun crearUsuario(@Body dto: UsuarioDto): Response<UsuarioDto>

    @PUT("usuario/{id}")
    suspend fun actualizarUsuario(@Path("id") id: Int, @Body dto: UsuarioDto): Response<UsuarioDto>

    @DELETE("usuario/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Int): Response<Void>

    @POST("usuario/login")
    suspend fun loginUsuarioAlt(@Body dto: LoginRequest): Response<String>


    // --- PERSONAL ---
    @GET("personal")
    suspend fun listarPersonal(): Response<List<PersonalDto>>

    @GET("personal/{id}")
    suspend fun obtenerPersonal(@Path("id") id: Int): Response<PersonalDto>

    @POST("personal")
    suspend fun crearPersonal(@Body dto: PersonalDto): Response<PersonalDto>

    @PUT("personal/{id}")
    suspend fun actualizarPersonal(@Path("id") id: Int, @Body dto: PersonalDto): Response<PersonalDto>

    @DELETE("personal/{id}")
    suspend fun eliminarPersonal(@Path("id") id: Int): Response<Void>


    // --- ASISTENCIA ---
    @GET("asistencia")
    suspend fun listarAsistencia(): Response<List<AsistenciaDto>>

    @POST("asistencia")
    suspend fun crearAsistencia(@Body dto: AsistenciaCreateRequest): Response<AsistenciaDto>

    @DELETE("asistencia/{id}")
    suspend fun eliminarAsistencia(@Path("id") id: Int): Response<Void>


    // --- AUTORIZACIÓN ---
    @GET("autorizacion")
    suspend fun listarAutorizacion(): Response<List<AutorizacionDto>>

    @POST("autorizacion")
    suspend fun crearAutorizacion(@Body dto: AutorizacionDto): Response<AutorizacionDto>

    @DELETE("autorizacion/{id}")
    suspend fun eliminarAutorizacion(@Path("id") id: Int): Response<Void>


    // --- MOVIMIENTO ---
    @GET("movimiento")
    suspend fun listarMovimiento(): Response<List<MovimientoDto>>

    @POST("movimiento")
    suspend fun crearMovimiento(@Body dto: MovimientoDto): Response<MovimientoDto>

    @DELETE("movimiento/{id}")
    suspend fun eliminarMovimiento(@Path("id") id: Int): Response<Void>


    // --- DOCUMENTO ---
    @GET("documento")
    suspend fun listarDocumentos(): Response<List<DocumentoDto>>

    @POST("documento")
    suspend fun crearDocumento(@Body dto: DocumentoDto): Response<DocumentoDto>

    @DELETE("documento/{id}")
    suspend fun eliminarDocumento(@Path("id") id: Int): Response<Void>


    // --- CARGO ---
    @GET("cargo")
    suspend fun listarCargo(): Response<List<CargoDto>>

    @GET("cargo/{id}")
    suspend fun obtenerCargo(@Path("id") id: Int): Response<CargoDto>

    @POST("cargo")
    suspend fun crearCargo(@Body dto: CargoDto): Response<CargoDto>

    @PUT("cargo/{id}")
    suspend fun actualizarCargo(@Path("id") id: Int, @Body dto: CargoDto): Response<CargoDto>

    @DELETE("cargo/{id}")
    suspend fun eliminarCargo(@Path("id") id: Int): Response<Void>
}
