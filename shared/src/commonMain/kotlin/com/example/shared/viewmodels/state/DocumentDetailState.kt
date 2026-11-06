package com.example.shared.viewmodels.state

import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.DocumentType

data class DocumentDetailState(
    val document: DocumentRecord? = null,
    val apartments: List<Apartment> = emptyList(),
    val name: String = "",
    val type: DocumentType = DocumentType.OTHER,
    val apartmentId: String? = null,
    val nameError: String? = null,
    val isLoading: Boolean = true
) {
    val isDirty: Boolean
        get() {
            val d = document ?: return false
            return name.trim() != d.displayName || type != d.type || apartmentId != d.apartmentId.value
        }
}