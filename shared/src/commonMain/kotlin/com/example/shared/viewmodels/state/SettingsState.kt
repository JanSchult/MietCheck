package com.example.shared.viewmodels.state

data class SettingsState(
    val hasPin: Boolean,
    val biometricAvailable: Boolean,
    val biometricEnabled: Boolean,
    val timeoutSeconds: Int,
    val screenshotsAllowed: Boolean,
    val isExporting: Boolean = false
)