package com.example.shared.domain.platform

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.shared.domain.lock.AppLockManager
import com.example.shared.domain.model.mediaKindOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AndroidExportArchiveWriter(
    private val context: Context,
    private val lockManager: AppLockManager,
    private val vault: AndroidMediaVault
) : ExportArchiveWriter {

    private val dir = File(context.cacheDir, "shared")     // dasselbe Verzeichnis wie beim Teilen von Dokumenten
    private var file: File? = null
    private var zip: ZipOutputStream? = null

    override suspend fun begin(fileName: String) = withContext(Dispatchers.IO) {
        dir.mkdirs()
        dir.listFiles()?.forEach { it.delete() }            // alte temporäre Dateien entfernen
        val target = File(dir, fileName)
        file = target
        zip = ZipOutputStream(BufferedOutputStream(FileOutputStream(target)))
        zip!!.setLevel(java.util.zip.Deflater.BEST_SPEED)
    }

    override suspend fun addEntry(path: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        val stream = checkNotNull(zip) { "Das Archiv ist nicht geöffnet." }
        stream.putNextEntry(ZipEntry(path))
        stream.write(bytes)
        stream.closeEntry()
    }
    override suspend fun addMedia(pathWithoutExtension: String, storedName: String): String =
        withContext(Dispatchers.IO) {
            val stream = checkNotNull(zip) { "Das Archiv ist nicht geöffnet." }
            vault.openDecrypted(storedName).use { raw ->
                val input = BufferedInputStream(raw)
                input.mark(16)
                val head = ByteArray(12)
                val read = input.read(head).coerceAtLeast(0)
                input.reset()
                val extension = vault.sniff(mediaKindOf(storedName), head, read).first
                val path = "$pathWithoutExtension.$extension"
                stream.putNextEntry(ZipEntry(path))
                input.copyTo(stream)
                stream.closeEntry()
                path
            }
        }
    override suspend fun finishAndShare() {
        val archive = withContext(Dispatchers.IO) {
            zip?.close()
            zip = null
            checkNotNull(file) { "Das Archiv ist nicht geöffnet." }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archive)
        lockManager.beginExternalFlow()                      // Rückkehr aus dem Teilen-Menü soll nicht sperren
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, archive.name)
            clipData = ClipData.newRawUri(archive.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(send, null)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

    override suspend fun abort() = withContext(Dispatchers.IO) {
        runCatching { zip?.close() }
        zip = null
        file?.delete()
        file = null
    }
}