package com.example.shared.domain.platform

expect class PinHasher {
    fun randomSalt(): ByteArray
    suspend fun hash(pin: String, salt: ByteArray, iterations: Int): ByteArray
}