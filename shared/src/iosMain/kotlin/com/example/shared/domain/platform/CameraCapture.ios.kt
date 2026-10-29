package com.example.shared.domain.platform

actual class CameraCapture {
    actual suspend fun capturePhoto(): PlatformFileResult {
        // TODO: AVFoundation Integration (AVCaptureSession / AVCapturePhotoOutput)
        throw NotImplementedError("AVFoundation camera capture requires UIViewController integration.")
    }
}