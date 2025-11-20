package com.example.gestindeasistencia.data.repositorio

import android.content.Context
import com.example.gestindeasistencia.data.models.AsistenciaCreateRequest
import com.example.gestindeasistencia.data.models.AsistenciaDto
import com.example.gestindeasistencia.data.remote.ApiClient
import retrofit2.Response

class AsistenciaRepository(private val context: Context) {
    
    private val apiService = ApiClient.getClient(context)

    /**
     * Obtener lista de asistencias
     */
    suspend fun listarAsistencias(): Response<List<AsistenciaDto>> {
        return apiService.listarAsistencia()
    }

    /**
     * Marcar asistencia (entrada, salida, break, etc.)
     */
    suspend fun marcarAsistencia(request: AsistenciaCreateRequest): Response<AsistenciaDto> {
        return apiService.crearAsistencia(request)
    }

    /**
     * Eliminar asistencia
     */
    suspend fun eliminarAsistencia(id: Int): Response<Void> {
        return apiService.eliminarAsistencia(id)
    }
}
