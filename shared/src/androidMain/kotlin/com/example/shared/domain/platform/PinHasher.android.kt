package com.example.shared.domain.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

actual class PinHasher {
    actual fun randomSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }


    actual suspend fun hash(pin: String, salt: ByteArray, iterations: Int): ByteArray =
        withContext(Dispatchers.Default) {
            val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, 256)
            try {
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            } finally {
                spec.clearPassword()
            }
        }
}