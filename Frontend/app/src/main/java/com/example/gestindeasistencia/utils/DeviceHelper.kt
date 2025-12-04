package com.example.gestindeasistencia.utils

import android.os.Build

/**
 * Utilidades para detectar el tipo de dispositivo
 */
object DeviceHelper {
    
    /**
     * Detecta si el dispositivo es un emulador de Android Studio
     */
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
                || Build.HARDWARE.contains("ranchu")
                || Build.HARDWARE.contains("goldfish"))
    }
    
    /**
     * Determina si se debe pedir huella obligatoriamente
     * - Celular físico: SÍ (obligatoria)
     * - Emulador en DEBUG: NO (para pruebas)
     * - Emulador en RELEASE: SÍ (bloquea)
     */
    fun shouldRequireBiometric(): Boolean {
        // Si es emulador
        if (isEmulator()) {
            // En modo DEBUG, no requerir (para pruebas)
            // En modo RELEASE, requerir (bloquear)
            return !com.example.gestindeasistencia.BuildConfig.DEBUG
        }
        
        // Si es dispositivo físico, siempre requerir
        return true
    }
}

