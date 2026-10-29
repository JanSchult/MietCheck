package com.example.shared.data.repositorys

import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.DocumentId
import com.example.shared.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

// TODO: durch SQLDelight-Implementierung ersetzen
class InMemoryDocumentRepository : DocumentRepository {

    private val state = MutableStateFlow<List<DocumentRecord>>(emptyList())

    @OptIn(ExperimentalTime::class)
    override fun observeForApartment(apartmentId: ApartmentId): Flow<List<DocumentRecord>> =
        state.map { list ->
            list.filter { it.apartmentId == apartmentId }.sortedByDescending { it.importedAt }
        }

    override suspend fun getById(id: DocumentId): DocumentRecord? =
        state.value.find { it.id == id }

    override suspend fun save(document: DocumentRecord) {
        state.value = state.value.filterNot { it.id == document.id } + document
    }

    override suspend fun delete(id: DocumentId) {
        state.value = state.value.filterNot { it.id == id }
    }
}