package com.example.shared.viewmodels.state

import com.example.shared.domain.model.Reminder

data class ReminderListState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)