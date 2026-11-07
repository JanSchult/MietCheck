package com.example.shared.domain.platform

actual class PinHasher {
    actual fun randomSalt(): ByteArray = TODO("iOS: SecRandomCopyBytes")
    actual suspend fun hash(pin: String, salt: ByteArray, iterations: Int): ByteArray =
        TODO("iOS: CCKeyDerivationPBKDF")
}