package com.example.shared.domain.platform

import com.example.shared.domain.model.OcrText

class UnavailableOcrEngine : OcrEngine {
    override val isAvailable = false
    override suspend fun recognize(bytes: ByteArray, mimeType: String): OcrText =
        error("Die Texterkennung ist auf diesem Gerät nicht verfügbar.")
}