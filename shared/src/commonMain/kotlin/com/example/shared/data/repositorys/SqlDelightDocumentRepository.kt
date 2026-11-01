package com.example.shared.data.repositorys


import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.data.mapper.toDomain
import com.example.shared.data.mapper.toEntity
import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.DocumentId
import com.example.shared.domain.repository.DocumentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightDocumentRepository(
    private val db: MietCheckDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : DocumentRepository {

    private val q get() = db.documentQueries

    override fun observeForApartment(apartmentId: ApartmentId): Flow<List<DocumentRecord>> =
        q.selectDocumentsForApartment(apartmentId.value).asFlow().mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: DocumentId): DocumentRecord? = withContext(dispatcher) {
        q.selectDocumentById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(document: DocumentRecord) = withContext(dispatcher) {
        q.upsertDocument(document.toEntity())
    }

    override suspend fun delete(id: DocumentId) = withContext(dispatcher) {
        q.deleteDocument(id.value)
    }
}