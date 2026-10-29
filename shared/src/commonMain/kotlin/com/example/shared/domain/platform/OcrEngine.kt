package com.example.shared.domain.platform

expect class OcrEngine {
    suspend fun recognizeText(imagePath: String): OcrResult
}