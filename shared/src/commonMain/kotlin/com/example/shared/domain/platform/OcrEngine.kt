package com.example.shared.domain.platform

import com.example.shared.domain.model.OcrText

interface OcrEngine {
    val isAvailable: Boolean
    suspend fun recognize(bytes: ByteArray, mimeType: String): OcrText
}
