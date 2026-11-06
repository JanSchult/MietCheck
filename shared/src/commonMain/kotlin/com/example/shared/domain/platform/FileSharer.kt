package com.example.shared.domain.platform

interface FileSharer {
    suspend fun shareFile(bytes: ByteArray, fileName: String, mimeType: String)
}