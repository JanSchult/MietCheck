package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
 fun PadKey(label: String, enabled: Boolean, description: String? = null, onClick: () -> Unit) {
    val base = Modifier.size(width = 88.dp, height = 64.dp)
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = if (description != null) base.semantics { contentDescription = description } else base,
        contentPadding = PaddingValues(0.dp)
    ) { Text(label, style = MaterialTheme.typography.headlineSmall) }
}