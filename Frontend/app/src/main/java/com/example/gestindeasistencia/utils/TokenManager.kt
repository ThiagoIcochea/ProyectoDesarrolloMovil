package com.example.gestindeasistencia.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.google.gson.Gson
import java.nio.charset.StandardCharsets

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE)

    companion object {
        const val KEY_TOKEN = "jwt_token"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun getDecodedTokenPayload(): Map<String, Any> {
        val token = getToken() ?: return emptyMap()

        return try {
            val parts = token.split(".")
            if (parts.size < 2) return emptyMap()
            val payloadBase64 = parts[1]
            val decodedBytes = Base64.decode(payloadBase64, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes, StandardCharsets.UTF_8)

            Gson().fromJson(decodedString, Map::class.java) as Map<String, Any>

        } catch (e: Exception) {
            println("Error al decodificar el token JWT: ${e.message}")
            emptyMap()
        }
    }

    fun getUserId(): Int? {
        val id = getDecodedTokenPayload()["id"]
        return if (id is Double) id.toInt() else (id as? Int)
    }

    fun getUserRole(): String? {
        return getDecodedTokenPayload()["cargo"] as? String
    }
}