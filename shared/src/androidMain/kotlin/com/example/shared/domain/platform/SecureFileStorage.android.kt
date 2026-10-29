package com.example.shared.domain.platform

import android.content.Context
import java.io.File
import androidx.security.crypto.MasterKey

actual class SecureFileStorage(private val context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    actual suspend fun save(bytes: ByteArray, fileName: String): String {
        val file = File(context.filesDir, fileName)
        if (file.exists()) file.delete()

        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        encryptedFile.openFileOutput().use { it.write(bytes) }
        return file.absolutePath
    }

    actual suspend fun read(path: String): ByteArray {
        val file = File(path)
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        return encryptedFile.openFileInput().use { it.readBytes() }
    }

    actual suspend fun delete(path: String): Boolean {
        return File(path).delete()
    }
}