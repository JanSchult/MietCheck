package com.example.shared.domain.usecase

import com.example.shared.domain.model.ids.ReminderId
import com.example.shared.domain.model.nextAfter
import com.example.shared.domain.repository.ReminderRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class MarkReminderCompletedUseCase(private val repository: ReminderRepository) {
    suspend operator fun invoke(id: ReminderId, completed: Boolean) {
        val before = repository.getById(id) ?: return
        repository.markCompleted(id, completed)

        // Nächster Termin nur beim Wechsel offen -> erledigt
        if (!completed || before.isCompleted) return
        val next = before.recurrenceRule?.nextAfter(before.dueAt) ?: return
        repository.save(before.copy(id = ReminderId.new(), dueAt = next, isCompleted = false))
    }
}