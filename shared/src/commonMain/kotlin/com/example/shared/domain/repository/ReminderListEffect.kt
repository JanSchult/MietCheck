package com.example.shared.domain.repository

sealed interface ReminderListEffect {
    data object NavigateToAddReminder : ReminderListEffect
    data class ShowError(val message: String) : ReminderListEffect
}