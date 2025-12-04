package com.example.gestindeasistencia.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestindeasistencia.data.repositorio.AuthRepository
import com.example.gestindeasistencia.data.repositorio.UsuarioRepository
import com.example.gestindeasistencia.utils.JwtUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(
        val token: String,
        val username: String?,
        val cargo: String?,
        val id: Int?
    ) : LoginState()
    data class Error(val msg: String) : LoginState()
}

class LoginViewModel(private val context: Context) : ViewModel() {

    private val repo = AuthRepository(context)
    private val usuarioRepo = UsuarioRepository(context)

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(usuario: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            val result = repo.login(usuario, password)

            if (result.isSuccess) {
                val token = result.getOrNull()!!
                val payload = JwtUtils.decodePayload(token)

                if (payload == null) {
                    _state.value = LoginState.Error("Token inválido")
                    repo.logout()
                    return@launch
                }

                val username = payload.optString("sub", null)
                val cargo = payload.optString("cargo", null)
                val id = payload.optInt("id", -1).takeIf { it >= 0 }

                // VALIDACIÓN DE ESTADO: Obtener datos completos del usuario
                if (id != null) {
                    try {
                        val usuarioResult = usuarioRepo.obtenerUsuario(id)

                        if (usuarioResult.isSuccess) {
                            val usuarioDto = usuarioResult.getOrNull()

                            // Verificar si el usuario está activo
                            if (usuarioDto?.estado == "ACTIVO") {
                                _state.value = LoginState.Success(token, username, cargo, id)
                            } else {
                                // Usuario inactivo - cerrar sesión
                                repo.logout()
                                _state.value = LoginState.Error(
                                    "Usuario inactivo. Contacte al administrador."
                                )
                            }
                        } else {
                            // Error al obtener usuario - permitir login (fallback)
                            _state.value = LoginState.Success(token, username, cargo, id)
                        }
                    } catch (e: Exception) {
                        // Error en la validación - permitir login (fallback)
                        _state.value = LoginState.Success(token, username, cargo, id)
                    }
                } else {
                    // No hay ID en el token - permitir login
                    _state.value = LoginState.Success(token, username, cargo, id)
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Error desconocido"
                _state.value = LoginState.Error(
                    when {
                        errorMsg.contains("Credenciales inválidas") ||
                                errorMsg.contains("401") ||
                                errorMsg.contains("403") ->
                            "Usuario o contraseña incorrectos"
                        else ->
                            "Error de conexión: $errorMsg"
                    }
                )
            }
        }
    }

    fun logout() {
        repo.logout()
        _state.value = LoginState.Idle
    }

    companion object {
        fun Factory(context: android.content.Context) =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                        return LoginViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}