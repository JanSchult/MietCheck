package com.example.shared.data.repositorys


import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.LandlordContact
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.LandlordContactId
import com.example.shared.domain.repository.ApartmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// TODO: durch SQLDelight-Implementierung ersetzen (Interface bleibt gleich)
class InMemoryApartmentRepository : ApartmentRepository {

    private val apartments = MutableStateFlow<List<Apartment>>(emptyList())
    private val landlords = MutableStateFlow<List<LandlordContact>>(emptyList())

    override fun observeAll(): Flow<List<Apartment>> = apartments

    override suspend fun getById(id: ApartmentId): Apartment? =
        apartments.value.find { it.id == id }

    override suspend fun save(apartment: Apartment) {
        apartments.value = apartments.value.filterNot { it.id == apartment.id } + apartment
    }

    override suspend fun delete(id: ApartmentId) {
        apartments.value = apartments.value.filterNot { it.id == id }
    }

    override suspend fun getLandlord(id: LandlordContactId): LandlordContact? =
        landlords.value.find { it.id == id }

    override suspend fun saveLandlord(contact: LandlordContact) {
        landlords.value = landlords.value.filterNot { it.id == contact.id } + contact
    }
}