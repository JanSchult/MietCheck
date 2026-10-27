package com.example.shared.domain.repository

import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.LandlordContact
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.LandlordContactId
import kotlinx.coroutines.flow.Flow

interface ApartmentRepository {
    fun observeAll(): Flow<List<Apartment>>
    suspend fun getById(id: ApartmentId): Apartment?
    suspend fun save(apartment: Apartment)
    suspend fun delete(id: ApartmentId)

    suspend fun getLandlord(id: LandlordContactId): LandlordContact?
    suspend fun saveLandlord(contact: LandlordContact)
}