package com.example.shared.domain.platform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.shared.domain.lock.AppLockManager
import com.example.shared.domain.model.StoredMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File

@Composable
actual fun rememberMediaCapturer(
    onStored: (StoredMedia) -> Unit,
    onError: (String) -> Unit,
): MediaCapturer {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vault = koinInject<AndroidMediaVault>()
    val lockManager = koinInject<AppLockManager>()
    val latestStored by rememberUpdatedState(onStored)
    val latestError by rememberUpdatedState(onError)

    // Überlebt das Beenden der Activity, während die Kamera-App offen ist
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }

    fun ingest(uri: Uri, tempFile: File?) {
        scope.launch {
            try {
                vault.ingest(uri)
                    .onSuccess { latestStored(it) }
                    .onFailure { latestError(it.message ?: "Die Datei konnte nicht gespeichert werden.") }
            } finally {
                tempFile?.delete()               // unverschlüsselte Kamera-Kopie sofort entfernen
            }
        }
    }

    fun finishCapture(success: Boolean) {
        val file = pendingPath?.let(::File)
        pendingPath = null
        if (success && file != null && file.exists()) ingest(Uri.fromFile(file), file) else file?.delete()
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { finishCapture(it) }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { finishCapture(it) }
    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) ingest(uri, null)
    }

    fun newTempFile(suffix: String): File {
        val dir = File(context.cacheDir, "capture").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }             // Reste früherer Abbrüche löschen
        return File(dir, "capture-${System.currentTimeMillis()}$suffix")
    }

    fun launchCapture(suffix: String, launch: (Uri) -> Unit) {
        try {
            val file = newTempFile(suffix)
            pendingPath = file.absolutePath
            lockManager.beginExternalFlow()
            launch(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file))
        } catch (e: Exception) {
            pendingPath = null
            latestError("Die Kamera konnte nicht gestartet werden.")
        }
    }

    return remember {
        object : MediaCapturer {
            override fun takePhoto() = launchCapture(".jpg") { photoLauncher.launch(it) }
            override fun recordVideo() = launchCapture(".mp4") { videoLauncher.launch(it) }
            override fun pickFromGallery() {
                lockManager.beginExternalFlow()
                pickLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            }
        }
    }}

@Composable
actual fun EncryptedImage(storedName: String, modifier: Modifier, contentDescription: String?) {
    val vault = koinInject<AndroidMediaVault>()
    val bitmap by produceState<ImageBitmap?>(null, storedName) {
        value = withContext(Dispatchers.IO) {
            runCatching { vault.decodeThumbnail(storedName, 512)?.asImageBitmap() }.getOrNull()
        }
    }
    val image = bitmap
    if (image != null) {
        Image(image, contentDescription, modifier, contentScale = ContentScale.Crop)
    } else {
        Box(modifier, contentAlignment = Alignment.Center) { Text("…") }
    }
}