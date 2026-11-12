package com.example.shared.data.repositorys


import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.shared.data.local.DefectMediaEntity
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.data.mapper.toDomain
import com.example.shared.data.mapper.toEntity
import com.example.shared.domain.model.Communication
import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.DefectStatus
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.DefectId
import com.example.shared.domain.repository.DefectRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class SqlDelightDefectRepository(
    private val db: MietCheckDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : DefectRepository {

    private val q get() = db.defectQueries

    override fun observeForApartment(apartmentId: ApartmentId): Flow<List<Defect>> =
        combine(
            q.selectDefectsForApartment(apartmentId.value).asFlow().mapToList(dispatcher),
            q.selectMediaForApartment(apartmentId.value).asFlow().mapToList(dispatcher),
            q.selectCommunicationsForApartment(apartmentId.value).asFlow().mapToList(dispatcher)
        ) { defects, media, communications ->
            val mediaBy = media.groupBy { it.defectId }
            val commsBy = communications.groupBy { it.defectId }
            defects.map { it.toDomain(mediaBy[it.id].orEmpty(), commsBy[it.id].orEmpty()) }
        }

    override fun observeById(id: DefectId): Flow<Defect?> =
        combine(
            q.selectDefectById(id.value).asFlow().mapToOneOrNull(dispatcher),
            q.selectMediaForDefect(id.value).asFlow().mapToList(dispatcher),
            q.selectCommunicationsForDefect(id.value).asFlow().mapToList(dispatcher)
        ) { defect, media, communications -> defect?.toDomain(media, communications) }

    override suspend fun getById(id: DefectId): Defect? = withContext(dispatcher) {
        val row = q.selectDefectById(id.value).executeAsOneOrNull() ?: return@withContext null
        row.toDomain(
            q.selectMediaForDefect(id.value).executeAsList(),
            q.selectCommunicationsForDefect(id.value).executeAsList()
        )
    }

    override suspend fun save(defect: Defect) = withContext(dispatcher) {
        db.transaction {
            q.upsertDefect(defect.toEntity())
            q.deleteMediaForDefect(defect.id.value)
            defect.mediaFilePaths.forEachIndexed { index, path ->
                q.insertMedia(DefectMediaEntity(defect.id.value, index.toLong(), path))
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateStatus(id: DefectId, status: DefectStatus, reportedAt: Instant?) =
        withContext(dispatcher) {
            q.setDefectStatus(status.name, reportedAt?.toEpochMilliseconds(), id.value)
        }

    override suspend fun addCommunication(communication: Communication) = withContext(dispatcher) {
        q.upsertCommunication(communication.toEntity())
    }

    override suspend fun delete(id: DefectId) = withContext(dispatcher) {
        db.transaction {
            q.deleteCommunicationsForDefect(id.value)
            q.deleteMediaForDefect(id.value)
            q.deleteDefect(id.value)
        }
    }
    override suspend fun addMedia(defectId: DefectId, storedName: String) = withContext(dispatcher) {
        val next = (q.selectMediaForDefect(defectId.value).executeAsList().maxOfOrNull { it.position } ?: -1L) + 1
        q.insertMedia(DefectMediaEntity(defectId.value, next, storedName))
    }

    override suspend fun removeMedia(defectId: DefectId, storedName: String) = withContext(dispatcher) {
        q.deleteMediaByPath(defectId.value, storedName)
    }
}