package com.example.shared.presentation.reminder

import com.example.shared.domain.model.RecurrenceRule
import com.example.shared.domain.model.ReminderType

sealed interface ReminderFormIntent {
    data class Init(val apartmentId: String) : ReminderFormIntent
    data class TitleChanged(val value: String) : ReminderFormIntent
    data class TypeChanged(val type: ReminderType) : ReminderFormIntent
    data class DueDateChanged(val value: String) : ReminderFormIntent
    data class RecurrenceChanged(val rule: RecurrenceRule) : ReminderFormIntent
    data object Save : ReminderFormIntent
}