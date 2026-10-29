package com.example.shared.domain.platform

expect class SecureFileStorage {
    suspend fun save(bytes: ByteArray, fileName: String): String // gibt lokalen Pfad zurück
    suspend fun read(path: String): ByteArray
    suspend fun delete(path: String): Boolean
}