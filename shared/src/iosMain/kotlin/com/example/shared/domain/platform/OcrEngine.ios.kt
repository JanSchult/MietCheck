package com.example.shared.domain.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import kotlin.coroutines.resume

actual class OcrEngine {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun recognizeText(imagePath: String): OcrResult = suspendCancellableCoroutine { continuation ->
        val imageUrl = NSURL.fileURLWithPath(imagePath)
        val request = VNRecognizeTextRequest { request, error ->
            if (error != null) {
                continuation.resume(OcrResult("", error.localizedDescription))
                return@VNRecognizeTextRequest
            }
            val observations = request?.results as? List<VNRecognizedTextObservation> ?: emptyList()
            val recognizedString =
                observations.mapNotNull { it.topCandidates(1u).first() as? VNRecognizedText }
                    .joinToString("\n") { it.string }

            continuation.resume(OcrResult(recognizedString, recognizedString))
        }

        request.recognitionLevel = VNRequestTextRecognitionLevelAccurate

        val handler = VNImageRequestHandler(uRL = imageUrl, options = emptyMap<Any?, Any?>())
        val success = handler.performRequests(listOf(request), error = null)
        if (!success) {
            continuation.resume(OcrResult("", "Failed to perform Vision request"))
        }
    }
}