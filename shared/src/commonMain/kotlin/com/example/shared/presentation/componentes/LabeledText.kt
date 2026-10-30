package com.example.shared.presentation.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun LabeledText(label: String, body: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(body, style = MaterialTheme.typography.bodyLarge)
    }
}