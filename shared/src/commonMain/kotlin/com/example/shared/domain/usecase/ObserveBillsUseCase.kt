package com.example.shared.domain.usecase

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.repository.UtilityBillRepository
import kotlinx.coroutines.flow.Flow

class ObserveBillsUseCase (private val repository: UtilityBillRepository) {
    operator fun invoke(apartmentId: ApartmentId): Flow<List<UtilityBill>> =
        repository.observeForApartment(apartmentId)
}