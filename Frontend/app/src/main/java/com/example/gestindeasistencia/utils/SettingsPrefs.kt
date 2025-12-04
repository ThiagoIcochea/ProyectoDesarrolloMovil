package com.example.gestindeasistencia.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Preferencias de configuración del usuario
 * Almacena configuraciones como tema, notificaciones, etc.
 */
object SettingsPrefs {

    private const val PREF_NAME = "settings_prefs"
    
    // Keys
    private const val KEY_DARK_THEME = "dark_theme"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_NOTIFICATION_ENTRADA = "notification_entrada"
    private const val KEY_NOTIFICATION_SALIDA = "notification_salida"
    private const val KEY_HORA_ENTRADA = "hora_entrada"
    private const val KEY_HORA_SALIDA = "hora_salida"
    private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"

    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ===== TEMA OSCURO =====
    fun isDarkTheme(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_DARK_THEME, false)
    }

    fun setDarkTheme(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    // ===== BIOMETRÍA =====
    fun isBiometricEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    // ===== NOTIFICACIÓN ENTRADA =====
    fun isNotificationEntradaEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_NOTIFICATION_ENTRADA, false)
    }

    fun setNotificationEntradaEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATION_ENTRADA, enabled).apply()
    }

    fun getHoraEntrada(context: Context): String {
        return prefs(context).getString(KEY_HORA_ENTRADA, "07:50") ?: "07:50"
    }

    fun setHoraEntrada(context: Context, hora: String) {
        prefs(context).edit().putString(KEY_HORA_ENTRADA, hora).apply()
    }

    // ===== NOTIFICACIÓN SALIDA =====
    fun isNotificationSalidaEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_NOTIFICATION_SALIDA, false)
    }

    fun setNotificationSalidaEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATION_SALIDA, enabled).apply()
    }

    fun getHoraSalida(context: Context): String {
        return prefs(context).getString(KEY_HORA_SALIDA, "17:50") ?: "17:50"
    }

    fun setHoraSalida(context: Context, hora: String) {
        prefs(context).edit().putString(KEY_HORA_SALIDA, hora).apply()
    }

    // ===== FOTO DE PERFIL =====
    fun getProfileImageUri(context: Context): String? {
        return prefs(context).getString(KEY_PROFILE_IMAGE_URI, null)
    }

    fun setProfileImageUri(context: Context, uri: String?) {
        prefs(context).edit().putString(KEY_PROFILE_IMAGE_URI, uri).apply()
    }

    // ===== NOMBRE PERSONALIZADO =====
    private const val KEY_CUSTOM_NAME = "custom_name"
    private const val KEY_CUSTOM_EMAIL = "custom_email"
    private const val KEY_CUSTOM_PHONE = "custom_phone"
    
    fun getCustomName(context: Context): String? {
        return prefs(context).getString(KEY_CUSTOM_NAME, null)
    }

    fun setCustomName(context: Context, name: String?) {
        prefs(context).edit().putString(KEY_CUSTOM_NAME, name).apply()
    }
    
    fun getCustomEmail(context: Context): String? {
        return prefs(context).getString(KEY_CUSTOM_EMAIL, null)
    }

    fun setCustomEmail(context: Context, email: String?) {
        prefs(context).edit().putString(KEY_CUSTOM_EMAIL, email).apply()
    }
    
    fun getCustomPhone(context: Context): String? {
        return prefs(context).getString(KEY_CUSTOM_PHONE, null)
    }

    fun setCustomPhone(context: Context, phone: String?) {
        prefs(context).edit().putString(KEY_CUSTOM_PHONE, phone).apply()
    }

    // ===== LIMPIAR TODO =====
    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}

