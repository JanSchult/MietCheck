// androidMain/kotlin/com/example/shared/domain/platform/BiometricAuthenticator.android.kt
package com.example.shared.domain.platform

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

actual class BiometricAuthenticator(
    private val activityProvider: CurrentActivityProvider
) {
    /** true nur bei erfolgreicher Biometrie. Abbruch, Fehler oder fehlende Biometrie ergeben false. */
    actual suspend fun authenticate(): Boolean {
        val activity = activityProvider.current ?: return false

        val availability = BiometricManager.from(activity).canAuthenticate(BIOMETRIC_WEAK)
        if (availability != BiometricManager.BIOMETRIC_SUCCESS) return false

        return suspendCancellableCoroutine { continuation ->
            val promptRef = AtomicReference<BiometricPrompt?>(null)

            activity.runOnUiThread {   // BiometricPrompt muss auf dem Main-Thread erstellt werden
                val callback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) continuation.resume(true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (continuation.isActive) continuation.resume(false)
                    }
                    // onAuthenticationFailed: z. B. falscher Finger, der Dialog bleibt offen
                }

                val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
                promptRef.set(prompt)

                val info = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("MietCheck entsperren")
                    .setNegativeButtonText("PIN verwenden")
                    .setAllowedAuthenticators(BIOMETRIC_WEAK)
                    .setConfirmationRequired(false)
                    .build()
                prompt.authenticate(info)
            }

            continuation.invokeOnCancellation {
                activity.runOnUiThread { promptRef.get()?.cancelAuthentication() }
            }
        }
    }
}