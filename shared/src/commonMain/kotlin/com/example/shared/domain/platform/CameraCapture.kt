package com.example.shared.domain.platform

expect class CameraCapture {
    suspend fun capturePhoto(): PlatformFileResult
}