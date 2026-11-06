package com.example.shared.presentation.componentes

import androidx.compose.runtime.Composable
import com.example.shared.domain.model.ImportedFile
import com.example.shared.domain.platform.DocumentImporter
import androidx.compose.runtime.remember

@Composable
actual fun rememberDocumentImporter(
    onImported: (ImportedFile) -> Unit,
    onError: (String) -> Unit
): DocumentImporter = remember {
    object : DocumentImporter {
        override val canPick = false
        override val canScan = false
        override fun pickFile() = onError("Der Dokumentenimport ist auf iOS noch nicht verfügbar.")
        override fun scan() = onError("Der Dokumentenimport ist auf iOS noch nicht verfügbar.")
    }
}