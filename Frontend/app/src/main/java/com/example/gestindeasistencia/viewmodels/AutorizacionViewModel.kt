package com.example.gestindeasistencia.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestindeasistencia.data.models.AutorizacionDto
import com.example.gestindeasistencia.data.models.MovimientoDto
import com.example.gestindeasistencia.data.models.UsuarioDto
import com.example.gestindeasistencia.data.repositorio.AutorizacionRepository
import com.example.gestindeasistencia.data.remote.ApiClient
import com.example.gestindeasistencia.utils.SecurePrefs
import com.example.gestindeasistencia.utils.JwtUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AutorizacionViewModel(private val context: Context) : ViewModel() {

    private val repo = AutorizacionRepository(context)
    private val apiService = ApiClient.getClient(context) // Para cargar movimientos

    private val _lista = MutableStateFlow<List<AutorizacionDto>>(emptyList())
    val lista: StateFlow<List<AutorizacionDto>> = _lista

    private val _movimientos = MutableStateFlow<List<MovimientoDto>>(emptyList())
    val movimientos: StateFlow<List<MovimientoDto>> = _movimientos

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    private val _operacionExitosa = MutableStateFlow(false)
    val operacionExitosa: StateFlow<Boolean> = _operacionExitosa

    fun cargarDatos() {
        viewModelScope.launch {
            _loading.value = true
            // Cargar autorizaciones
            val resAuth = repo.listar()
            if (resAuth.isSuccess) _lista.value = resAuth.getOrNull()!!.sortedByDescending { it.fechaSolicitud }

            // Cargar movimientos (tipos de permiso)
            try {
                val resMov = apiService.listarMovimiento()
                if (resMov.isSuccessful) {
                    _movimientos.value = resMov.body() ?: emptyList()
                }
            } catch (e: Exception) { e.printStackTrace() }

            _loading.value = false
        }
    }

    fun crearSolicitud(idMovimiento: Int, descripcion: String) {
        viewModelScope.launch {
            _loading.value = true
            val token = SecurePrefs.getToken(context)
            val userId = token?.let { JwtUtils.extractId(it) }

            if (userId != null) {
                val fechaNow = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

                // Construimos el objeto. Nota: Solo necesitamos enviar IDs en los objetos anidados
                // El backend espera la estructura completa, pero Spring a veces acepta solo ID.
                // Lo ideal es enviar objetos mínimos.
                val movimiento = _movimientos.value.find { it.idMovimiento == idMovimiento }

                val nuevaAuth = AutorizacionDto(
                    idAutorizacion = null,
                    movimiento = movimiento,
                    usuarioSolicita = UsuarioDto(idUsuario = userId, null, null, "", null, null, null),
                    usuarioAutoriza = null,
                    descripcion = descripcion,
                    fechaSolicitud = fechaNow,
                    fechaAprobacion = null,
                    estado = "PENDIENTE"
                )

                val result = repo.crear(nuevaAuth)
                if (result.isSuccess) {
                    _mensaje.value = "Solicitud enviada correctamente"
                    _operacionExitosa.value = true
                    cargarDatos() // Recargar lista
                } else {
                    _mensaje.value = "Error al enviar: ${result.exceptionOrNull()?.message}"
                }
            }
            _loading.value = false
        }
    }

    fun aprobar(id: Int) {
        viewModelScope.launch {
            val res = repo.aprobar(id)
            if (res.isSuccess) {
                _mensaje.value = "Solicitud Aprobada"
                cargarDatos()
            } else {
                _mensaje.value = "Error al aprobar"
            }
        }
    }

    fun rechazar(id: Int) {
        viewModelScope.launch {
            val res = repo.rechazar(id)
            if (res.isSuccess) {
                _mensaje.value = "Solicitud Rechazada"
                cargarDatos()
            } else {
                _mensaje.value = "Error al rechazar"
            }
        }
    }

    fun resetOperacion() {
        _operacionExitosa.value = false
        _mensaje.value = null
    }

    companion object {
        fun Factory(context: Context) = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AutorizacionViewModel(context) as T
            }
        }
    }
}