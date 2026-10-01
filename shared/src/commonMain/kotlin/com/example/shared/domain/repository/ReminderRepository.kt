package com.example.shared.domain.repository

import com.example.shared.domain.model.Reminder
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.ReminderId
import kotlinx.coroutines.flow.Flow


interface ReminderRepository {
    fun observeForApartment(apartmentId: ApartmentId): Flow<List<Reminder>>
    suspend fun getById(id: ReminderId): Reminder?
    suspend fun save(reminder: Reminder)
    suspend fun markCompleted(id: ReminderId, completed: Boolean)
    suspend fun delete(id: ReminderId)
}