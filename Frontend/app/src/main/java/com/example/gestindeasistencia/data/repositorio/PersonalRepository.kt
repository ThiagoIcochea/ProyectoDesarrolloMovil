package com.example.gestindeasistencia.data.repositorio

import android.content.Context
import com.example.gestindeasistencia.data.models.PersonalDto
import com.example.gestindeasistencia.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersonalRepository(context: Context) {

    private val api = ApiClient.getClient(context)

    suspend fun listarPersonal(): Result<List<PersonalDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.listarPersonal()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("Error ${response.code()} al listar personal"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun obtenerPersonal(id: Int): Result<PersonalDto> =
        try {
            val resp = api.obtenerPersonal(id)
            if (resp.isSuccessful) Result.success(resp.body()!!)
            else Result.failure(Exception("Error ${resp.code()} al obtener personal"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun crearPersonal(dto: PersonalDto): Result<PersonalDto> =
        try {
            val resp = api.crearPersonal(dto)
            if (resp.isSuccessful) Result.success(resp.body()!!)
            else Result.failure(Exception("Error al crear personal"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun actualizarPersonal(id: Int, dto: PersonalDto): Result<PersonalDto> =
        try {
            val resp = api.actualizarPersonal(id, dto)
            if (resp.isSuccessful) Result.success(resp.body()!!)
            else Result.failure(Exception("Error al actualizar"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun eliminarPersonal(id: Int): Result<Boolean> =
        try {
            val resp = api.eliminarPersonal(id)
            Result.success(resp.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
