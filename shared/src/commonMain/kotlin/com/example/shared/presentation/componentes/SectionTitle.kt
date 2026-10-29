package com.example.shared.presentation.componentes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
 fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge)
}