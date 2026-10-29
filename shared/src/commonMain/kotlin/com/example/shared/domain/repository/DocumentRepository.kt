package com.example.shared.domain.repository

import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.DocumentId
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeForApartment(apartmentId: ApartmentId): Flow<List<DocumentRecord>>
    suspend fun getById(id: DocumentId): DocumentRecord?
    suspend fun save(document: DocumentRecord)
    suspend fun delete(id: DocumentId)
}