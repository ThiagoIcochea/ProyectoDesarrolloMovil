package com.example.gestindeasistencia.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestindeasistencia.data.models.PersonalDto
import com.example.gestindeasistencia.data.models.UsuarioDto
import com.example.gestindeasistencia.data.repositorio.PersonalRepository
import com.example.gestindeasistencia.data.repositorio.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsuarioViewModel(context: Context): ViewModel() {
    private val repo = UsuarioRepository(context)
    private val personalRepo = PersonalRepository(context)

    private val _lista = MutableStateFlow<List<UsuarioDto>>(emptyList())
    val lista: StateFlow<List<UsuarioDto>> = _lista

    private val _loading = MutableStateFlow(false)
    val loading : StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _detalle = MutableStateFlow<UsuarioDto?>(null)
    val detalle: StateFlow<UsuarioDto?> = _detalle

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _listaPersonal = MutableStateFlow<List<PersonalDto>>(emptyList())
    val listaPersonal: StateFlow<List<PersonalDto>> = _listaPersonal.asStateFlow()

    fun crearUsuario(dto: UsuarioDto){
        viewModelScope.launch {
            _loading.value = true
            val result = repo.crearUsuario(dto)
            _loading.value = false

            if (result.isSuccess) {
                _saveSuccess.value = true
            } else {
                _error.value = "Error al crear usuario: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun listarUsuarios(){
        viewModelScope.launch {
            _loading.value = true
            val result = repo.listarUsuarios()
            _loading.value = false

            if (result.isSuccess){
                _lista.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun cargaUsuario(id: Int){
        viewModelScope.launch {
            _loading.value = true
            val result = repo.obtenerUsuario(id)
            _loading.value = false

            if (result.isSuccess){
                _detalle.value = result.getOrNull()
            } else {
                _error.value = "Error cargando el usuario: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun actualizarUsuario(id: Int, dto: UsuarioDto){
        viewModelScope.launch {
            _loading.value = true
            val result = repo.actualizarUsuario(id, dto)
            _loading.value = false

            if (result.isSuccess) {
                _saveSuccess.value = true
            } else {
                _error.value = "Error al actualizar usuario: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun eliminar(id: Int){
        viewModelScope.launch {
            _loading.value = true
            val result = repo.eliminarUsuario(id)
            _loading.value = false

            if (result.isSuccess) {
                _deleteSuccess.value = true
            } else {
                _error.value = "Error al eliminar usuario: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // ← NUEVA FUNCIÓN: Cambiar estado del usuario
    fun cambiarEstadoUsuario(id: Int, nuevoEstado: String) {
        viewModelScope.launch {
            _loading.value = true
            val resultUsuario = repo.obtenerUsuario(id)

            if (resultUsuario.isSuccess) {
                val usuarioActual = resultUsuario.getOrNull()
                usuarioActual?.let { usuario ->
                    val usuarioActualizado = usuario.copy(estado = nuevoEstado)
                    val result = repo.actualizarUsuario(id, usuarioActualizado)
                    _loading.value = false

                    if (result.isSuccess) {
                        _saveSuccess.value = true
                    } else {
                        _error.value = "Error al cambiar estado: ${result.exceptionOrNull()?.message}"
                    }
                }
            } else {
                _loading.value = false
                _error.value = "Error al obtener usuario: ${resultUsuario.exceptionOrNull()?.message}"
            }
        }
    }

    fun cargarListaPersonal() {
        viewModelScope.launch {
            val result = personalRepo.listarPersonal()

            if (result.isSuccess) {
                _listaPersonal.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = "Error al cargar personal: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    fun resetDeleteSuccess() {
        _deleteSuccess.value = false
    }

    fun resetError() {
        _error.value = null
    }

    fun resetDetalle() {
        _detalle.value = null
    }

    companion object {
        fun Factory(context: Context) =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return UsuarioViewModel(context) as T
                }
            }
    }
}