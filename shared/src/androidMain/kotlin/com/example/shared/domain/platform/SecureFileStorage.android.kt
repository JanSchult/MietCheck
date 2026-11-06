// androidMain: domain/platform/SecureFileStorage.android.kt (Datei ersetzen)
package com.example.shared.domain.platform

import android.content.Context
import com.example.shared.data.local.DatabaseKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

actual class SecureFileStorage(context: Context) {

    private val appContext = context.applicationContext
    private val dir: File by lazy { File(appContext.filesDir, "documents").also { it.mkdirs() } }
    private val fileKey: SecretKeySpec by lazy {
        SecretKeySpec(DatabaseKeyProvider(appContext).getOrCreateFileKey(), "AES")
    }

    actual suspend fun save(bytes: ByteArray, fileName: String): String = withContext(Dispatchers.IO) {
        val target = resolve(fileName)
        val temp = File(dir, "$fileName.tmp")
        temp.writeBytes(encrypt(bytes))
        check(temp.renameTo(target)) { "Datei konnte nicht gespeichert werden." }
        fileName
    }

    actual suspend fun read(path: String): ByteArray = withContext(Dispatchers.IO) {
        decrypt(resolve(path).readBytes())
    }

    actual suspend fun delete(path: String): Boolean = withContext(Dispatchers.IO) {
        resolve(path).delete()
    }

    /** Nur generierte Namen zulassen: schützt vor Pfadtricks wie "../". */
    private fun resolve(name: String): File {
        require(SAFE_NAME.matches(name)) { "Ungültiger Dateiname." }
        return File(dir, name)
    }

    private fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, fileKey) }
        return cipher.iv + cipher.doFinal(plain)               // 12 Byte IV + Ciphertext mit Tag
    }

    private fun decrypt(blob: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, fileKey, GCMParameterSpec(128, blob.copyOfRange(0, IV_SIZE)))
        }
        return cipher.doFinal(blob, IV_SIZE, blob.size - IV_SIZE)
    }

    private companion object {
        val SAFE_NAME = Regex("[A-Za-z0-9-]{1,64}\\.bin")
        const val IV_SIZE = 12
    }
}