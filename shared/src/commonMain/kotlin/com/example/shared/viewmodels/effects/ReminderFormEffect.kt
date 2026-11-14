package com.example.shared.viewmodels.effects

sealed interface ReminderFormEffect {
    data object Saved : ReminderFormEffect
    data class ShowError(val message: String) : ReminderFormEffect
}