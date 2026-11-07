package com.example.shared.domain.platform

expect class BiometricAuthenticator {
    fun isAvailable(): Boolean
    suspend fun authenticate(): Boolean
}