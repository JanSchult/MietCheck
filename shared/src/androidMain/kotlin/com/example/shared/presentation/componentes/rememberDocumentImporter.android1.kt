package com.example.shared.presentation.componentes

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.example.shared.domain.lock.AppLockManager
import com.example.shared.domain.model.ImportedFile
import com.example.shared.domain.model.MAX_IMPORT_BYTES
import com.example.shared.domain.model.detectMimeType
import com.example.shared.domain.platform.DocumentImporter
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
actual fun rememberDocumentImporter(
    onImported: (ImportedFile) -> Unit,
    onError: (String) -> Unit,
): DocumentImporter {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val latestImported by rememberUpdatedState(onImported)
    val latestError by rememberUpdatedState(onError)
    val lockManager = koinInject<AppLockManager>()
    fun deliver(uri: Uri, pageCount: Int?, fallbackName: String?, deleteAfter: Boolean) {
        scope.launch {
            readImport(context, uri, pageCount, fallbackName)
                .onSuccess { latestImported(it) }
                .onFailure { latestError(it.message ?: "Die Datei konnte nicht gelesen werden.") }
            if (deleteAfter) deleteTempFile(uri)
        }
    }

    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) deliver(uri, pageCount = null, fallbackName = null, deleteAfter = false)
    }

    val scanLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val pdf = GmsDocumentScanningResult.fromActivityResultIntent(result.data)?.pdf
            if (pdf != null) deliver(pdf.uri, pdf.pageCount, scanName(), deleteAfter = true)
            else latestError("Der Scan konnte nicht gelesen werden.")
        }
    }

    return remember {
        object : DocumentImporter {
            override val canPick = true
            override val canScan = true

            override fun pickFile() {
                lockManager.beginExternalFlow()
                pickLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png"))
            }

            override fun scan() {
                lockManager.beginExternalFlow()
                val activity = context.findActivity()
                if (activity == null) {
                    latestError("Der Scanner konnte nicht gestartet werden.")
                    return
                }
                val options = GmsDocumentScannerOptions.Builder()
                    .setGalleryImportAllowed(true)
                    .setPageLimit(10)
                    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                    .build()
                GmsDocumentScanning.getClient(options)
                    .getStartScanIntent(activity)
                    .addOnSuccessListener { sender ->
                        scanLauncher.launch(IntentSenderRequest.Builder(sender).build())
                    }
                    .addOnFailureListener {
                        latestError("Der Scanner ist auf diesem Gerät nicht verfügbar. Wähle stattdessen eine Datei aus.")
                    }
            }
        }
    }
}

private suspend fun readImport(
    context: Context,
    uri: Uri,
    pageCount: Int?,
    fallbackName: String?
): Result<ImportedFile> = withContext(Dispatchers.IO) {
    runCatching {
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { readLimited(it, MAX_IMPORT_BYTES) }
            ?: error("Die Datei konnte nicht geöffnet werden.")
        val mime = detectMimeType(bytes)
            ?: error("Dieses Dateiformat wird nicht unterstützt. Erlaubt sind PDF, JPEG und PNG.")

        val pages = pageCount ?: if (mime == "application/pdf") pdfPageCount(resolver, uri) else 1
        val name = fallbackName
            ?: displayName(resolver, uri)?.substringBeforeLast('.')
            ?: "Dokument"

        ImportedFile(bytes, mime, name, pages, sha256Hex(bytes))
    }
}

private fun readLimited(input: InputStream, max: Long): ByteArray {
    val out = ByteArrayOutputStream()
    val buffer = ByteArray(8 * 1024)
    var total = 0L
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        total += read
        if (total > max) error("Die Datei ist größer als 25 MB.")
        out.write(buffer, 0, read)
    }
    return out.toByteArray()
}

private fun displayName(resolver: ContentResolver, uri: Uri): String? = runCatching {
    resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
}.getOrNull()

private fun pdfPageCount(resolver: ContentResolver, uri: Uri): Int = runCatching {
    resolver.openFileDescriptor(uri, "r")?.use { pfd ->
        val renderer = PdfRenderer(pfd)
        try { renderer.pageCount } finally { renderer.close() }
    } ?: 1
}.getOrDefault(1)

private fun sha256Hex(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

private fun scanName(): String = "Scan " + SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date())

/** Der Scanner legt sein PDF in den App-Cache. Nach dem Import wird es gelöscht. */
private fun deleteTempFile(uri: Uri) {
    if (uri.scheme == "file") uri.path?.let { File(it).delete() }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}