package com.example.shared.domain.platform


import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.example.shared.domain.model.StoredMedia
import com.example.shared.domain.model.MediaKind
import com.example.shared.data.local.DatabaseKeyProvider
import com.example.shared.domain.lock.AppLockManager
import com.example.shared.domain.model.MAX_PHOTO_BYTES
import com.example.shared.domain.model.MAX_VIDEO_BYTES
import com.example.shared.domain.model.mediaKindOf
import com.example.shared.domain.repository.MediaVault
import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.subtle.AesGcmHkdfStreaming
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class AndroidMediaVault(
    context: Context,
    private val lockManager: AppLockManager
) : MediaVault {

    private val appContext = context.applicationContext
    private val dir = File(appContext.filesDir, "media").also { it.mkdirs() }
    private val shareDir = File(appContext.cacheDir, "shared")     // wie beim Teilen von Dokumenten

    // Schlüssel pro Datei wird per HKDF aus dem Dateischlüssel abgeleitet
    private val streaming: StreamingAead by lazy {
        AesGcmHkdfStreaming(DatabaseKeyProvider(appContext).getOrCreateFileKey(), "HmacSha256", 32, 64 * 1024, 0)
    }

    /** Liest die Quelle in Blöcken, prüft den Inhalt (Magic Bytes) und legt sie verschlüsselt ab. */
    suspend fun ingest(uri: Uri): Result<StoredMedia> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = appContext.contentResolver.openInputStream(uri)
                ?: error("Die Datei konnte nicht geöffnet werden.")
            raw.use { source ->
                val input = BufferedInputStream(source)
                input.mark(32)
                val head = ByteArray(16)
                val read = input.read(head).coerceAtLeast(0)
                input.reset()

                val kind = detectKind(head, read)
                    ?: error("Dieses Format wird nicht unterstützt. Erlaubt sind Fotos (JPEG, PNG) und Videos (MP4, MOV).")
                val limit = if (kind == MediaKind.PHOTO) MAX_PHOTO_BYTES else MAX_VIDEO_BYTES
                val name = (if (kind == MediaKind.PHOTO) "p-" else "v-") + UUID.randomUUID() + ".bin"
                val target = resolve(name)

                var total = 0L
                try {
                    // Dateiname als zugehörige Daten: eine vertauschte Datei lässt sich nicht entschlüsseln
                    streaming.newEncryptingStream(FileOutputStream(target), name.toByteArray()).use { out ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            check(total <= limit) { "Die Datei ist größer als ${limit / (1024 * 1024)} MB." }
                            out.write(buffer, 0, count)
                        }
                    }
                } catch (e: Throwable) {
                    target.delete()
                    throw e
                }
                StoredMedia(name, kind, total)
            }
        }
    }

    fun openDecrypted(storedName: String): InputStream =
        streaming.newDecryptingStream(FileInputStream(resolve(storedName)), storedName.toByteArray())

    /** Verkleinertes Vorschaubild, mit Korrektur der Kameradrehung (EXIF). */
    fun decodeThumbnail(storedName: String, maxDimension: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openDecrypted(storedName).use { BitmapFactory.decodeStream(it, null, bounds) }

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxDimension && bounds.outHeight / (sample * 2) >= maxDimension) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = openDecrypted(storedName).use { BitmapFactory.decodeStream(it, null, options) } ?: return null

        val orientation = runCatching {
            openDecrypted(storedName).use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        return if (degrees == 0f) bitmap else Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true
        )
    }

    override suspend fun open(storedName: String, share: Boolean) {
        val (file, mime) = withContext(Dispatchers.IO) { decryptToCache(storedName) }
        val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)
        lockManager.beginExternalFlow()                       // Rückkehr aus Galerie/Teilen soll nicht sperren

        val intent = if (share) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            Intent.createChooser(send, null)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        try {
            appContext.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            throw IllegalStateException("Es wurde keine App zum Ansehen gefunden.")
        }
    }

    override suspend fun delete(storedName: String) {
        withContext(Dispatchers.IO) { runCatching { resolve(storedName).delete() } }
    }

    /** Dateiendung und MIME-Typ anhand der ersten Bytes. */
    fun sniff(kind: MediaKind, head: ByteArray, count: Int): Pair<String, String> = when {
        kind == MediaKind.VIDEO ->
            if (count >= 12 && String(head, 8, 4, Charsets.ISO_8859_1) == "qt  ") "mov" to "video/quicktime"
            else "mp4" to "video/mp4"
        count >= 4 && head[0] == 0x89.toByte() -> "png" to "image/png"
        else -> "jpg" to "image/jpeg"
    }

    private fun decryptToCache(storedName: String): Pair<File, String> {
        shareDir.mkdirs()
        shareDir.listFiles()?.forEach { it.delete() }           // immer nur eine temporäre Datei
        openDecrypted(storedName).use { raw ->
            val input = BufferedInputStream(raw)
            input.mark(16)
            val head = ByteArray(12)
            val read = input.read(head).coerceAtLeast(0)
            input.reset()
            val (ext, mime) = sniff(mediaKindOf(storedName), head, read)
            val out = File(shareDir, "MietCheck-Mangel-${storedName.substring(2, 10)}.$ext")
            FileOutputStream(out).use { input.copyTo(it) }
            return out to mime
        }
    }

    private fun detectKind(h: ByteArray, count: Int): MediaKind? = when {
        count >= 3 && h[0] == 0xFF.toByte() && h[1] == 0xD8.toByte() && h[2] == 0xFF.toByte() -> MediaKind.PHOTO
        count >= 4 && h[0] == 0x89.toByte() && h[1] == 'P'.code.toByte() &&
                h[2] == 'N'.code.toByte() && h[3] == 'G'.code.toByte() -> MediaKind.PHOTO
        count >= 12 && h[4] == 'f'.code.toByte() && h[5] == 't'.code.toByte() &&
                h[6] == 'y'.code.toByte() && h[7] == 'p'.code.toByte() -> {
            // HEIC/HEIF haben denselben Container, werden hier bewusst nicht als Video erkannt
            val brand = String(h, 8, 4, Charsets.ISO_8859_1)
            if (brand in VIDEO_BRANDS || brand.startsWith("3g")) MediaKind.VIDEO else null
        }
        else -> null
    }

    private fun resolve(name: String): File {
        require(SAFE_NAME.matches(name)) { "Ungültiger Dateiname." }
        return File(dir, name)
    }

    private companion object {
        val SAFE_NAME = Regex("[pv]-[0-9a-fA-F-]{36}\\.bin")
        val VIDEO_BRANDS = setOf("isom", "iso2", "mp41", "mp42", "avc1", "qt  ", "M4V ", "MSNV")
    }
}