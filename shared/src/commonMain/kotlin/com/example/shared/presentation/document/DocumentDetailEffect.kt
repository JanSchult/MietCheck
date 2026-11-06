package com.example.shared.presentation.document

sealed interface DocumentDetailEffect {
    data object Deleted : DocumentDetailEffect
    data class ShowMessage(val message: String) : DocumentDetailEffect
}