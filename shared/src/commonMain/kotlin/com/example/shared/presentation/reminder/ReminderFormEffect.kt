package com.example.shared.presentation.reminder

sealed interface ReminderFormEffect {
    data object Saved : ReminderFormEffect
    data class ShowError(val message: String) : ReminderFormEffect
}