package com.example.shared.domain.model

import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.DefectId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Defect @OptIn(ExperimentalTime::class) constructor(
    val id: DefectId,
    val apartmentId: ApartmentId,
    val room: String,
    val description: String,
    val urgency: DefectUrgency,
    val status: DefectStatus,
    val mediaFilePaths: List<String>,
    val reportedAt: Instant?,
    val createdAt: Instant,
    val communications: List<Communication>
)