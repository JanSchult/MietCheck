package com.example.shared.domain.repository

import com.example.shared.domain.model.Reminder
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.ReminderId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

class InMemoryReminderRepository : ReminderRepository {

    private val state = MutableStateFlow<List<Reminder>>(emptyList())

    override fun observeForApartment(apartmentId: ApartmentId): Flow<List<Reminder>> =
        state.asStateFlow().map { list -> list.filter { it.apartmentId == apartmentId } }

    override suspend fun getById(id: ReminderId): Reminder? =
        state.value.find { it.id == id }

    override suspend fun save(reminder: Reminder) {
        state.value = state.value.filterNot { it.id == reminder.id } + reminder
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun markCompleted(id: ReminderId, completed: Boolean) {
        state.value = state.value.map {
            if (it.id == id) it.copy(isCompleted = completed) else it
        }
    }

    override suspend fun delete(id: ReminderId) {
        state.value = state.value.filterNot { it.id == id }
    }
}