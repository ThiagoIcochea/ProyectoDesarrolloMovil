package com.example.gestindeasistencia.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    /**
     * Decodifica el payload (segunda parte) del JWT y devuelve un JSONObject.
     * Si hay error devuelve JSONObject vacío.
     */
    fun decodePayload(token: String): JSONObject {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return JSONObject()
            val payload64 = parts[1]
            // Base64 url-safe
            val decodedBytes = Base64.decode(payload64, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val json = String(decodedBytes, Charsets.UTF_8)
            JSONObject(json)
        } catch (e: Exception) {
            JSONObject()
        }
    }
}