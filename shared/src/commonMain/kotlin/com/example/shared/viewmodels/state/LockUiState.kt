package com.example.shared.viewmodels.state

data class LockUiState(
    val enteredDigits: Int = 0,
    val error: String? = null,
    val lockoutSeconds: Long = 0,
    val biometricAvailable: Boolean = false,
    val isChecking: Boolean = false
)