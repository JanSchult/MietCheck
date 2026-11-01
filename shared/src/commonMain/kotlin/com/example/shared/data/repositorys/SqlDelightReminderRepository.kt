package com.example.shared.data.repositorys


import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.data.mapper.toDomain
import com.example.shared.data.mapper.toEntity
import com.example.shared.domain.model.Reminder
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.ReminderId
import com.example.shared.domain.repository.ReminderRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightReminderRepository(
    private val db: MietCheckDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ReminderRepository {

    private val q get() = db.reminderQueries

    override fun observeForApartment(apartmentId: ApartmentId): Flow<List<Reminder>> =
        q.selectRemindersForApartment(apartmentId.value).asFlow().mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: ReminderId): Reminder? = withContext(dispatcher) {
        q.selectReminderById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(reminder: Reminder) = withContext(dispatcher) {
        q.upsertReminder(reminder.toEntity())
    }

    override suspend fun markCompleted(id: ReminderId, completed: Boolean) = withContext(dispatcher) {
        q.setReminderCompleted(completed, id.value)
    }

    override suspend fun delete(id: ReminderId) = withContext(dispatcher) {
        q.deleteReminder(id.value)
    }
}