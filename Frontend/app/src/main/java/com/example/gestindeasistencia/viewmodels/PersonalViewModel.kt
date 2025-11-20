package com.example.gestindeasistencia.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestindeasistencia.data.models.PersonalDto
import com.example.gestindeasistencia.data.repositorio.PersonalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PersonalViewModel(context: Context) : ViewModel() {

    private val repo = PersonalRepository(context)

    private val _lista = MutableStateFlow<List<PersonalDto>>(emptyList())
    val lista: StateFlow<List<PersonalDto>> = _lista

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun cargarPersonal() {
        viewModelScope.launch {
            _loading.value = true
            val result = repo.listarPersonal()
            _loading.value = false

            if (result.isSuccess) {
                _lista.value = result.getOrNull()!!
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    companion object {
        fun Factory(context: Context) =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PersonalViewModel(context) as T
                }
            }
    }
}
