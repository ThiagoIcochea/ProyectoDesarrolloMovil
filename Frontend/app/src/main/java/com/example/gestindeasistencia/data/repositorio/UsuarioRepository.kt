package com.example.gestindeasistencia.data.repositorio

import android.content.Context
import com.example.gestindeasistencia.data.models.UsuarioDto
import com.example.gestindeasistencia.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class UsuarioRepository(context: Context) {

    private val api = ApiClient.getClient(context)

    suspend fun listarUsuarios(): Result<List<UsuarioDto>> =
        withContext(Dispatchers.IO){
            try {
                val response = api.listarUsuarios()
                if (response.isSuccessful){
                    Result.success(response.body()?: emptyList())
                }else{
                    Result.failure(Exception("Error ${response.code()} al listar usuariios"))
                }
            }catch (e: Exception){
                Result.failure(e)
            }
        }

    suspend fun obtenerUsuario(id: Int): Result<UsuarioDto> =
        try {
            val resp = api.obtenerUsuario(id)
            if(resp.isSuccessful) Result.success(resp.body()!!)
            else Result.failure(Exception("Error ${resp.code()} al obtener usuario"))
        }catch (e: Exception){
            Result.failure(e)
        }

    suspend fun crearUsuario(dto: UsuarioDto): Result<UsuarioDto> =
    try{
        val resp = api.crearUsuario(dto)
        if(resp.isSuccessful) Result.success(resp.body()!!)
        else Result.failure(Exception("Error al crear usuario ${resp.code()} "))
    }catch (e: Exception){
        Result.failure(e)
    }

    suspend fun actualizarUsuario(id : Int, dto : UsuarioDto): Result<UsuarioDto> =
        try {
            val resp = api.actualizarUsuario(id, dto)
            if(resp.isSuccessful) Result.success( resp.body()!!)
            else Result.failure(Exception("Error al actualizar : ${resp.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    suspend fun eliminarUsuario(id: Int): Result<Boolean> =
        try {
            val resp = api.eliminarUsuario(id)
            Result.success(resp.isSuccessful)
        }catch (e: Exception){
            Result.failure(e)
        }


}