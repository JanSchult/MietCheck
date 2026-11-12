package com.example.shared.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportDocument(
    val id: String,
    val type: String,
    val name: String,
    val pages: Int,
    val importedAt: String,
    val file: String?          // Pfad im Archiv, null = Datei konnte nicht gelesen werden
)