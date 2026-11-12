package com.example.shared.domain.export

import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.StoredMedia
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class ExportDefect(
    val room: String,
    val description: String,
    val urgency: String,
    val status: String,
    val createdAt: String,
    val reportedAt: String?,
    val communications: List<ExportCommunication>,
    val media:List<String>
)
