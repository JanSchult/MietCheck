package com.example.shared.domain.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class BiometricAuthenticator {
    actual suspend fun authenticate(): Boolean = suspendCancellableCoroutine { continuation ->
        val context = LAContext()
        var error: kotlinx.cinterop.CPointer<out kotlinx.cinterop.CPointed>? = null

        if (context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error = null)) {
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = "Identität bestätigen, um fortzufahren"
            ) { success, _ ->
                continuation.resume(success)
            }
        } else {
            continuation.resume(false)
        }
    }
}

// Hilfsfunktionen für ByteArray <-> NSData Konvertierung unter iOS
@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData(): NSData = this.usePinned { pinned ->
    NSData.dataWithBytes(pinned.addressOf(0), length = this.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val bytes = this.bytes ?: return ByteArray(0)
    val length = this.length.toInt()
    val array = ByteArray(length)
    array.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), bytes, length.toULong())
    }
    return array
}