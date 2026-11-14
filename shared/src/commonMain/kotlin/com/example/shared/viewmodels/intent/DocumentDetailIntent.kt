package com.example.shared.viewmodels.intent

import com.example.shared.domain.model.DocumentType

sealed interface DocumentDetailIntent {
    data class Load(val documentId: String) : DocumentDetailIntent
    data class NameChanged(val value: String) : DocumentDetailIntent
    data class TypeSelected(val type: DocumentType) : DocumentDetailIntent
    data class ApartmentSelected(val apartmentId: String) : DocumentDetailIntent
    data object Save : DocumentDetailIntent
    data object Share : DocumentDetailIntent
    data object DeleteConfirmed : DocumentDetailIntent
}