package com.example.gestindeasistencia.data.repositorio

import android.content.Context
import com.example.gestindeasistencia.data.models.LoginRequest
import com.example.gestindeasistencia.data.remote.ApiClient
import com.example.gestindeasistencia.utils.SecurePrefs

class AuthRepository(private val context: Context) {

    private val api = ApiClient.getClient(context)

    suspend fun login(usuario: String, password: String): Result<String> {
        return try {
            val resp = api.login(LoginRequest(usuario, password))
            if (resp.isSuccessful) {
                val token = resp.body()
                if (!token.isNullOrBlank()) {
                    SecurePrefs.saveToken(context, token)
                    Result.success(token)
                } else {
                    Result.failure(Exception("Token vacío"))
                }
            } else {
                Result.failure(Exception("Credenciales inválidas: ${resp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        SecurePrefs.clear(context)
    }

    fun getSavedToken(): String? = SecurePrefs.getToken(context)
}