package com.example.gestindeasistencia.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File

object SecurePrefs {

    private const val PREF_NAME = "secure_prefs"
    private const val KEY_TOKEN = "jwt_token"

    /**
     * Obtiene la instancia de SharedPreferences de forma segura.
     * Si ocurre un error de desencriptación (corrupción), borra el archivo y crea uno nuevo.
     */
    private fun getPrefs(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            createEncryptedPrefs(context, masterKeyAlias)
        } catch (e: Exception) {
            // Si falla (ej. clave corrupta), borramos el archivo y reintentamos
            deleteSharedPreferencesFile(context, PREF_NAME)
            createEncryptedPrefs(context, masterKeyAlias)
        }
    }

    private fun createEncryptedPrefs(context: Context, masterKey: MasterKey): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Borra físicamente el archivo XML de preferencias
     */
    private fun deleteSharedPreferencesFile(context: Context, prefName: String) {
        try {
            // Intenta borrar usando la API (Android N+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                context.deleteSharedPreferences(prefName)
            } else {
                // Borrado manual para versiones antiguas o si la API falla
                val dir = File(context.filesDir.parent, "shared_prefs")
                val file = File(dir, "$prefName.xml")
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Métodos Públicos ---

    fun saveToken(context: Context, token: String) {
        try {
            getPrefs(context).edit().putString(KEY_TOKEN, token).apply()
        } catch (e: Exception) {
            // Si falla al guardar, intentamos limpiar y reintentar una vez más
            deleteSharedPreferencesFile(context, PREF_NAME)
            try {
                getPrefs(context).edit().putString(KEY_TOKEN, token).apply()
            } catch (e2: Exception) {
                e2.printStackTrace() // Si falla de nuevo, solo lo logueamos
            }
        }
    }

    fun getToken(context: Context): String? {
        return try {
            getPrefs(context).getString(KEY_TOKEN, null)
        } catch (e: Exception) {
            // Si no se puede leer, asumimos que no hay token (y borramos el archivo corrupto por seguridad)
            deleteSharedPreferencesFile(context, PREF_NAME)
            null
        }
    }

    fun clear(context: Context) {
        try {
            getPrefs(context).edit().clear().apply()
        } catch (e: Exception) {
            // El error que tuviste ocurrió aquí.
            // Si no se puede limpiar la preferencia, borramos el archivo entero.
            deleteSharedPreferencesFile(context, PREF_NAME)
        }
    }
}