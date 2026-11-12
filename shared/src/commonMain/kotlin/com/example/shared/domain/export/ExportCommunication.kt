package com.example.shared.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportCommunication(val direction: String, val summary: String, val occurredAt: String)