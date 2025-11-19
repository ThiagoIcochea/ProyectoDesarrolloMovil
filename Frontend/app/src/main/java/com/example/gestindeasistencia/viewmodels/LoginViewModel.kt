package com.example.gestindeasistencia.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestindeasistencia.data.repositorio.AuthRepository
import com.example.gestindeasistencia.utils.JwtUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val token: String, val username: String?, val cargo: String?, val id: Int?) : LoginState()
    data class Error(val msg: String) : LoginState()
}

class LoginViewModel(private val context: Context) : ViewModel() {

    private val repo = AuthRepository(context)

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(usuario: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            val result = repo.login(usuario, password)
            if (result.isSuccess) {
                val token = result.getOrNull()!!
                val payload = JwtUtils.decodePayload(token)
                val username = payload.optString("sub", null)
                val cargo = payload.optString("cargo", null)
                val id = if (payload.has("id")) payload.optInt("id", -1).takeIf { it >= 0 } else null
                _state.value = LoginState.Success(token, username, cargo, id)
            } else {
                _state.value = LoginState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        repo.logout()
        _state.value = LoginState.Idle
    }
}