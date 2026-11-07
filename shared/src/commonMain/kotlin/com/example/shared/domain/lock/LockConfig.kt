package com.example.shared.domain.lock

data class LockConfig(
    val onboardingDone: Boolean = false,
    val pinHash: String? = null,
    val pinSalt: String? = null,
    val pinIterations: Int = 0,
    val biometricEnabled: Boolean = false,
    val timeoutSeconds: Int = DEFAULT_TIMEOUT_SECONDS,
    val screenshotsAllowed: Boolean = false,
    val failedAttempts: Int = 0,
    val lockoutUntilMillis: Long = 0L
) {
    val hasPin: Boolean get() = pinHash != null
}