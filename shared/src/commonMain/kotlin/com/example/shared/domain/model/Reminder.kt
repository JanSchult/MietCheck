package com.example.shared.domain.model

import com.example.shared.model.ids.ApartmentId
import com.example.shared.model.ids.ReminderId
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Reminder @OptIn(ExperimentalTime::class) constructor(
    val id: ReminderId,
    val apartmentId: ApartmentId,
    val type: ReminderType,
    val title: String,
    val dueAt: Instant,
    val recurrenceRule: RecurrenceRule?,
    val isCompleted: Boolean,
    val linkedEntityId: String? // z.B. UtilityBillId
)