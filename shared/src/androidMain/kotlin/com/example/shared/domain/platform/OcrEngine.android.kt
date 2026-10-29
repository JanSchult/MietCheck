package com.example.shared.domain.platform

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.io.File

actual class OcrEngine {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    actual suspend fun recognizeText(imagePath: String): OcrResult = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(android.net.Uri.fromFile(File(imagePath)), android.content.Context.MODE_PRIVATE) // oder über File-URI
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(OcrResult(text = visionText.text, rawText = visionText.text))
                }
                .addOnFailureListener { e ->
                    continuation.resume(OcrResult(text = "", rawText = e.localizedMessage ?: "Unknown error"))
                }
        } catch (e: Exception) {
            continuation.resume(OcrResult(text = "", rawText = e.localizedMessage ?: "Exception"))
        }
    }
}