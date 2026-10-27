package com.example.shared.domain.usecase

import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.LandlordContact
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.repository.ApartmentRepository
import kotlinx.coroutines.flow.Flow

class ObserveApartmentsUseCase(private val repository: ApartmentRepository) {
    operator fun invoke(): Flow<List<Apartment>> = repository.observeAll()
}

class GetApartmentUseCase(private val repository: ApartmentRepository) {
    suspend operator fun invoke(id: ApartmentId): Apartment? = repository.getById(id)
}

class SaveApartmentUseCase(private val repository: ApartmentRepository) {
    /** Landlord zuerst speichern, damit die Referenz im Apartment gültig ist. */
    suspend operator fun invoke(apartment: Apartment, landlord: LandlordContact?) {
        landlord?.let { repository.saveLandlord(it) }
        repository.save(apartment)
    }
}

class DeleteApartmentUseCase(private val repository: ApartmentRepository) {
    suspend operator fun invoke(id: ApartmentId) = repository.delete(id)
}