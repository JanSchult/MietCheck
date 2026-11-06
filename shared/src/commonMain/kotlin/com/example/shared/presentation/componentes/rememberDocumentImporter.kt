package com.example.shared.presentation.componentes

import androidx.compose.runtime.Composable
import com.example.shared.domain.model.ImportedFile
import com.example.shared.domain.platform.DocumentImporter

@Composable
expect fun rememberDocumentImporter(
    onImported: (ImportedFile) -> Unit,
    onError: (String) -> Unit
): DocumentImporter