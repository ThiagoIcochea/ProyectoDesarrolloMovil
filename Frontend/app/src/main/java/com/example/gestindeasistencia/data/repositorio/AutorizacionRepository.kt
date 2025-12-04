package com.example.gestindeasistencia.data.repositorio

import android.content.Context
import com.example.gestindeasistencia.data.models.AutorizacionDto
import com.example.gestindeasistencia.data.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AutorizacionRepository(context: Context) {

    private val api = ApiClient.getClient(context)

    suspend fun listar(): Result<List<AutorizacionDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.listarAutorizacion()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crear(dto: AutorizacionDto): Result<AutorizacionDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.crearAutorizacion(dto)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al crear: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aprobar(id: Int): Result<AutorizacionDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.aprobarAutorizacion(id)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al aprobar"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rechazar(id: Int): Result<AutorizacionDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.rechazarAutorizacion(id)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Error al rechazar"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}