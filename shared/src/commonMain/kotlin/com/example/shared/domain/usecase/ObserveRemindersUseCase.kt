package com.example.shared.domain.usecase

import com.example.shared.domain.model.Reminder
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow

class ObserveRemindersUseCase(private val repository: ReminderRepository) {
    operator fun invoke(apartmentId: ApartmentId): Flow<List<Reminder>> =
        repository.observeForApartment(apartmentId)
}



