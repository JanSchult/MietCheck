package com.example.shared.domain.usecase

import com.example.shared.domain.model.Reminder
import com.example.shared.domain.repository.ReminderRepository

class SaveReminderUseCase(private val repository: ReminderRepository) {
    suspend operator fun invoke(reminder: Reminder) = repository.save(reminder)
}