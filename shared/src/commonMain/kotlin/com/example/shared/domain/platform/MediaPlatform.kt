package com.example.shared.domain.platform


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.shared.domain.model.StoredMedia

interface MediaCapturer {
    fun takePhoto()
    fun recordVideo()
    fun pickFromGallery()
}

@Composable
expect fun rememberMediaCapturer(
    onStored: (StoredMedia) -> Unit,
    onError: (String) -> Unit
): MediaCapturer

/** Zeigt ein verschlüsselt gespeichertes Foto als Vorschaubild. */
@Composable
expect fun EncryptedImage(storedName: String, modifier: Modifier, contentDescription: String?)