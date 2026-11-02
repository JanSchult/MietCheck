package com.example.shared.domain.platform

import android.content.Context
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.io.File

actual class OcrEngine(private val context: Context) { // Kontext im Konstruktor
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    actual suspend fun recognizeText(imagePath: String): OcrResult = suspendCancellableCoroutine { continuation ->
        try {
            // Korrekter Aufruf: Context an erster, Uri an zweiter Stelle
            val image = InputImage.fromFilePath(context, android.net.Uri.fromFile(File(imagePath)))

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