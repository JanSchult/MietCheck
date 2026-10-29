package com.example.shared.domain.model

import com.example.shared.domain.model.ids.CommunicationId
import com.example.shared.domain.model.ids.DefectId
import com.example.shared.domain.model.ids.DocumentId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Communication @OptIn(ExperimentalTime::class) constructor(
    val id: CommunicationId,
    val defectId: DefectId?,
    val direction: CommunicationDirection, // OUTGOING / INCOMING
    val summary: String,
    val documentId: DocumentId?,
    val occurredAt: Instant
)
