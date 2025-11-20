package com.example.gestindeasistencia.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestindeasistencia.data.models.*
import com.example.gestindeasistencia.data.repositorio.AsistenciaRepository
import com.example.gestindeasistencia.data.remote.ApiClient
import com.example.gestindeasistencia.utils.JwtUtils
import com.example.gestindeasistencia.utils.SecurePrefs
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AsistenciaViewModel(private val context: Context) : ViewModel() {

    private val repository = AsistenciaRepository(context)
    private val apiService = ApiClient.getClient(context)

    // Estados
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)
    var successMessage = mutableStateOf<String?>(null)
    var movimientos = mutableStateOf<List<MovimientoDto>>(emptyList())
    var asistencias = mutableStateOf<List<AsistenciaDto>>(emptyList())

    /**
     * Carga los movimientos disponibles del backend
     */
    fun cargarMovimientos() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val response = apiService.listarMovimiento()
                if (response.isSuccessful && response.body() != null) {
                    val movimientosList = response.body()!!.filter { it.estado == "ACTIVO" }
                    movimientos.value = movimientosList
                    
                    // Si no hay movimientos en el backend, crear algunos por defecto
                    if (movimientosList.isEmpty()) {
                        // Usar movimientos por defecto sin consultar al backend
                        movimientos.value = listOf(
                            MovimientoDto(1, "Entrada", "ENT", "ACTIVO"),
                            MovimientoDto(2, "Salida", "SAL", "ACTIVO"),
                            MovimientoDto(3, "Entrada Break", "EBR", "ACTIVO"),
                            MovimientoDto(4, "Fin Break", "FBR", "ACTIVO")
                        )
                    }
                } else {
                    // Si falla, usar movimientos por defecto
                    movimientos.value = listOf(
                        MovimientoDto(1, "Entrada", "ENT", "ACTIVO"),
                        MovimientoDto(2, "Salida", "SAL", "ACTIVO"),
                        MovimientoDto(3, "Entrada Break", "EBR", "ACTIVO"),
                        MovimientoDto(4, "Fin Break", "FBR", "ACTIVO")
                    )
                }
            } catch (e: Exception) {
                // En caso de error, usar movimientos por defecto
                movimientos.value = listOf(
                    MovimientoDto(1, "Entrada", "ENT", "ACTIVO"),
                    MovimientoDto(2, "Salida", "SAL", "ACTIVO"),
                    MovimientoDto(3, "Entrada Break", "EBR", "ACTIVO"),
                    MovimientoDto(4, "Fin Break", "FBR", "ACTIVO")
                )
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Carga las asistencias del empleado actual
     */
    fun cargarAsistencias() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val response = repository.listarAsistencias()
                if (response.isSuccessful && response.body() != null) {
                    asistencias.value = response.body()!!
                }
            } catch (e: Exception) {
                // Silenciosamente ignorar errores en la carga
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Marca la asistencia con los datos proporcionados
     */
    fun marcarAsistencia(
        personalId: Int,
        movimientoId: Int,
        ipConLocation: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            successMessage.value = null

            try {
                // Intentar obtener datos del personal
                var personal: PersonalDto? = null
                try {
                    val personalResponse = apiService.obtenerPersonal(personalId)
                    if (personalResponse.isSuccessful && personalResponse.body() != null) {
                        personal = personalResponse.body()!!
                    }
                } catch (e: Exception) {
                    // Ignorar error, crear personal mínimo
                }

                // Si no se pudo obtener el personal, crear uno mínimo
                if (personal == null) {
                    personal = PersonalDto(
                        idPersonal = personalId,
                        cargo = null,
                        documento = null,
                        nombre = null,
                        apellPaterno = null,
                        apellMaterno = null,
                        nroDocumento = null,
                        fechaNacimiento = null,
                        fechaIngreso = null,
                        email = null
                    )
                }

                // Buscar el movimiento en la lista cargada
                val movimiento = movimientos.value.find { it.idMovimiento == movimientoId }
                if (movimiento == null) {
                    errorMessage.value = "Movimiento no encontrado"
                    onError("Movimiento no encontrado")
                    isLoading.value = false
                    return@launch
                }

                // Obtener fecha y hora actual
                val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date())

                // Crear request
                val request = AsistenciaCreateRequest(
                    personal = personal,
                    movimiento = movimiento,
                    fecha = fechaActual,
                    ipMarcador = ipConLocation,
                    autorizacion = null
                )

                // Enviar al backend
                val response = repository.marcarAsistencia(request)
                if (response.isSuccessful) {
                    successMessage.value = "✓ ${movimiento.descripcion} marcada correctamente"
                    // Esperar y recargar múltiples veces para asegurar actualización
                    kotlinx.coroutines.delay(300)
                    cargarAsistencias()
                    kotlinx.coroutines.delay(500)
                    cargarAsistencias()
                    onSuccess()
                } else {
                    val error = "Error al marcar asistencia: ${response.code()}"
                    errorMessage.value = error
                    onError(error)
                }
            } catch (e: Exception) {
                val error = "Error de conexión: ${e.message}"
                errorMessage.value = error
                onError(error)
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Obtiene el ID del personal del token JWT
     */
    fun obtenerPersonalIdDeToken(): Int? {
        val token = SecurePrefs.getToken(context)
        return if (token != null) {
            JwtUtils.extractId(token)
        } else {
            null
        }
    }

    /**
     * Limpia los mensajes de error y éxito
     */
    fun limpiarMensajes() {
        errorMessage.value = null
        successMessage.value = null
    }
}
