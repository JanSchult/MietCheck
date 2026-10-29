package com.example.shared.presentation.componentes

import androidx.compose.runtime.Composable

@Composable
 fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge)
}