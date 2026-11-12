package com.example.shared.domain.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.shared.domain.model.StoredMedia

@Composable
actual fun rememberMediaCapturer(onStored: (StoredMedia) -> Unit, onError: (String) -> Unit): MediaCapturer =
    remember {
        object : MediaCapturer {
            private fun unavailable() = onError("Fotos und Videos sind auf iOS noch nicht verfügbar.")
            override fun takePhoto() = unavailable()
            override fun recordVideo() = unavailable()
            override fun pickFromGallery() = unavailable()
        }
    }


@Composable
actual fun EncryptedImage(storedName: String, modifier: Modifier, contentDescription: String?) {
    Box(modifier, contentAlignment = Alignment.Center) { Text("Foto") }
}