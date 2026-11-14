package com.example.shared.domain.platform

interface PdfTextWriter {
    suspend fun write(text: String): ByteArray
}