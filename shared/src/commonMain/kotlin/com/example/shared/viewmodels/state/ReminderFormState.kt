package com.example.shared.viewmodels.state

import com.example.shared.domain.model.RecurrenceRule
import com.example.shared.domain.model.ReminderType

data class ReminderFormState(
    val title: String = "",
    val type: ReminderType = ReminderType.SELF_SET_DEADLINE,
    val dueDate: String = "",
    val recurrence: RecurrenceRule = RecurrenceRule.NONE,
    val dateError: String? = null,
    val isSaving: Boolean = false
)