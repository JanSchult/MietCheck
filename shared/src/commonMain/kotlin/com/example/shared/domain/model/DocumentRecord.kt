package com.example.shared.domain.model

import com.example.shared.model.ids.ApartmentId
import com.example.shared.model.ids.DocumentId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class DocumentRecord @OptIn(ExperimentalTime::class) constructor(
    val id: DocumentId,
    val apartmentId: ApartmentId,
    val type: DocumentType,
    val displayName: String,
    val filePathLocal: String,
    val pageCount: Int,
    val importedAt: Instant,
    val sourceHash: String // Integritätsprüfung, Duplikaterkennung
)