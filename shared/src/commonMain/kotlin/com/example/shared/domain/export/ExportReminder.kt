package com.example.shared.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportReminder(
    val type: String,
    val title: String,
    val dueAt: String,
    val recurrence: String,
    val completed: Boolean
)