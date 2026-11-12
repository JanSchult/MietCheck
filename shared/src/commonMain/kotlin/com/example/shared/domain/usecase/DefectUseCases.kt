package com.example.shared.domain.usecase


import com.example.shared.domain.model.Communication
import com.example.shared.domain.model.CommunicationDirection
import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.DefectStatus
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.CommunicationId
import com.example.shared.domain.model.ids.DefectId
import com.example.shared.domain.repository.DefectRepository
import com.example.shared.domain.repository.MediaVault
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ObserveDefectsUseCase(private val repository: DefectRepository) {
    operator fun invoke(apartmentId: ApartmentId): Flow<List<Defect>> =
        repository.observeForApartment(apartmentId)
}

class ObserveDefectUseCase(private val repository: DefectRepository) {
    operator fun invoke(id: DefectId): Flow<Defect?> = repository.observeById(id)
}

class SaveDefectUseCase(private val repository: DefectRepository) {
    suspend operator fun invoke(defect: Defect) = repository.save(defect)
}

/** Beim ersten Wechsel auf "Gemeldet" wird das Meldedatum automatisch gesetzt. */
class UpdateDefectStatusUseCase(private val repository: DefectRepository) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(id: DefectId, status: DefectStatus) {
        val defect = repository.getById(id) ?: return
        val reportedAt = if (status == DefectStatus.REPORTED && defect.reportedAt == null) {
            Clock.System.now()
        } else defect.reportedAt
        repository.updateStatus(id, status, reportedAt)
    }
}

class AddCommunicationUseCase(private val repository: DefectRepository) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(defectId: DefectId, direction: CommunicationDirection, summary: String) {
        val text = summary.trim()
        if (text.isEmpty()) return
        repository.addCommunication(
            Communication(
                id = CommunicationId.new(),
                defectId = defectId,
                direction = direction,
                summary = text,
                documentId = null,
                occurredAt = Clock.System.now()
            )
        )
    }
}

class DeleteDefectUseCase(
    private val repository: DefectRepository,
    private val vault: MediaVault
) {
    suspend operator fun invoke(id: DefectId) {
        val media = repository.getById(id)?.mediaFilePaths.orEmpty()
        repository.delete(id)
        media.forEach { runCatching { vault.delete(it) } }
    }
}