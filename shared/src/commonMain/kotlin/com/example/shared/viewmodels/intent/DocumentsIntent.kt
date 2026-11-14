package com.example.shared.viewmodels.intent

import com.example.shared.domain.model.DocumentType
import com.example.shared.domain.model.ImportedFile

sealed interface DocumentsIntent {
    data class FilterChanged(val type: DocumentType?) : DocumentsIntent
    data class DocumentClicked(val id: String) : DocumentsIntent
    data class Import(val file: ImportedFile, val type: DocumentType) : DocumentsIntent
}