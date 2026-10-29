package com.example.shared.domain.platform

expect class BiometricAuthenticator {
    suspend fun authenticate(): Boolean
}