package com.example.shared.domain.platform

actual class CameraCapture {
    actual suspend fun capturePhoto(): PlatformFileResult {
        TODO("Not yet implemented")
        throw NotImplementedError("CameraX implementation requires Activity/Fragment lifecycle integration.")
    }
}