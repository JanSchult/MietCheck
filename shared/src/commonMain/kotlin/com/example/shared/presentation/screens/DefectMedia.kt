package com.example.shared.presentation.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.shared.domain.model.MediaKind
import com.example.shared.domain.model.mediaKindOf
import com.example.shared.domain.platform.EncryptedImage

@Composable
fun MediaThumbnail(storedName: String, modifier: Modifier = Modifier) {
    if (mediaKindOf(storedName) == MediaKind.VIDEO) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Text("▶ Video", style = MaterialTheme.typography.labelLarge)
        }
    } else {
        EncryptedImage(storedName, modifier, "Foto zum Mangel")
    }
}