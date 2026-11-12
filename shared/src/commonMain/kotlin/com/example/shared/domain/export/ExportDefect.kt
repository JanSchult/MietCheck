package com.example.shared.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportDefect(
    val room: String,
    val description: String,
    val urgency: String,
    val status: String,
    val createdAt: String,
    val reportedAt: String?,
    val communications: List<ExportCommunication>
)