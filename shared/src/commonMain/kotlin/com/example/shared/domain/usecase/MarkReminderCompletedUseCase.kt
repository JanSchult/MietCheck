package com.example.shared.domain.usecase

import com.example.shared.domain.model.ids.ReminderId
import com.example.shared.domain.repository.ReminderRepository

class MarkReminderCompletedUseCase(private val repository: ReminderRepository) {
    suspend operator fun invoke(id: ReminderId, completed: Boolean) =
        repository.markCompleted(id, completed)
}