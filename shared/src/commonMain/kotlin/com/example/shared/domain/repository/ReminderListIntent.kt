package com.example.shared.domain.repository

import com.example.shared.domain.model.ids.ReminderId

sealed interface ReminderListIntent {
    data class LoadReminders(val apartmentId: String) : ReminderListIntent
    data class ToggleCompleted(val id: ReminderId, val completed: Boolean) : ReminderListIntent
    data class DeleteReminder(val id: ReminderId) : ReminderListIntent
    data object AddReminderClicked : ReminderListIntent
}