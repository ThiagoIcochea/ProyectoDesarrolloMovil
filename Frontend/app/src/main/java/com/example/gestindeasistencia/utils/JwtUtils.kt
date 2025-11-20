package com.example.gestindeasistencia.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    fun decodePayload(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payloadJson = String(Base64.decode(parts[1], Base64.URL_SAFE))
            JSONObject(payloadJson)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserName(token: String): String {
        return decodePayload(token)?.optString("sub", "") ?: ""
    }

    fun getCargo(token: String): String {
        return decodePayload(token)?.optString("cargo", "") ?: ""
    }

    fun extractId(token: String): Int? {
        return try {
            decodePayload(token)?.optInt("id")
        } catch (e: Exception) {
            null
        }
    }
}