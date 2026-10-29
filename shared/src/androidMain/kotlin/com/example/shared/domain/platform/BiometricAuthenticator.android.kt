package com.example.shared.domain.platform

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class BiometricAuthenticator(private val context: Context) {
    actual suspend fun authenticate(): Boolean = suspendCancellableCoroutine { continuation ->
        // Hinweis: BiometricPrompt benötigt typischerweise eine FragmentActivity.
        // In KMP wird dies meist über einen Callback oder eine Callback-Registrierung gelöst.
        // Beispiel-Skizze:

        val executor = androidx.core.content.ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity as FragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    continuation.resume(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    continuation.resume(false)
                }

                override fun onAuthenticationFailed() {
                    // Optional: Bei Fehlschlag nicht direkt abbrechen
                }
            }
        )
        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometrische Authentifizierung")
            .setSubtitle("Identität bestätigen")
            .setNegativeButtonText("Abbrechen")
            .build()
        biometricPrompt.authenticate(promptInfo)

        continuation.resume(false) // Platzhalter bis zur Activity-Bindung
    }
}