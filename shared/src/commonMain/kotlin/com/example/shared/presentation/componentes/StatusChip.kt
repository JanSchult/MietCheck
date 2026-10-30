package com.example.shared.presentation.componentes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.shared.domain.model.StatusKind

/** Farbe plus Symbol plus Text: Der Status ist nie nur über die Farbe erkennbar. */
@Composable
fun StatusChip(kind: StatusKind, text: String, modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val (bg, fg) = when (kind) {
        StatusKind.OK ->
            if (dark) Color(0xFF1F3B22) to Color(0xFFB7E1B9) else Color(0xFFE3F1E4) to Color(0xFF1B5E20)
        StatusKind.NOTE ->
            if (dark) Color(0xFF4A3A10) to Color(0xFFFFE2A0) else Color(0xFFFFF1D6) to Color(0xFF6B4A00)
        StatusKind.ATTENTION ->
            if (dark) Color(0xFF4D2A14) to Color(0xFFFFCDAE) else Color(0xFFFFE3D1) to Color(0xFF8A3B00)
    }
    val symbol = when (kind) {
        StatusKind.OK -> "✓"
        StatusKind.NOTE -> "i"
        StatusKind.ATTENTION -> "!"
    }
    Surface(color = bg, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Text(
            text = "$symbol  $text",
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}