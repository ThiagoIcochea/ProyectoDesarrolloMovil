package com.example.gestindeasistencia.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val context: Context) {

    /**
     * Verifica si el dispositivo tiene autenticación biométrica disponible
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Muestra el prompt de autenticación biométrica
     * @param activity Activity desde donde se llama
     * @param title Título del diálogo
     * @param subtitle Subtítulo del diálogo
     * @param onSuccess Callback cuando la autenticación es exitosa
     * @param onError Callback cuando hay un error
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Autenticación requerida",
        subtitle: String = "Coloca tu huella digital para continuar",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Autenticación fallida. Inténtalo de nuevo.")
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    companion object {
        /**
         * Obtiene un mensaje descriptivo del estado de la biometría
         */
        fun getBiometricStatusMessage(context: Context): String {
            val biometricManager = BiometricManager.from(context)
            return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS ->
                    "Autenticación biométrica disponible"
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    "Este dispositivo no tiene sensor de huella"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    "El sensor de huella no está disponible"
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                    "No hay huellas registradas en el dispositivo"
                else -> "Estado de biometría desconocido"
            }
        }
    }
}

