package com.example.shared.domain.platform


import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.shared.domain.lock.AppLockManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidFileSharer(private val context: Context, private val lockManager: AppLockManager) : FileSharer {

    private val dir = File(context.cacheDir, "shared")

    init { clear() }      // Reste vom letzten App-Lauf löschen

    private fun clear() { dir.listFiles()?.forEach { it.delete() } }

    override suspend fun shareFile(bytes: ByteArray, fileName: String, mimeType: String) {
        lockManager.beginExternalFlow()

        val file = withContext(Dispatchers.IO) {
            dir.mkdirs()
            clear()                                  // immer nur eine temporäre Datei
            File(dir, fileName).also { it.writeBytes(bytes) }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            clipData = ClipData.newRawUri(fileName, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(send, null)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }
}