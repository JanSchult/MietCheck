package com.example.shared.domain.repository


import com.example.shared.domain.model.Communication
import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.DefectStatus
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.DefectId
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
interface DefectRepository {
    fun observeForApartment(apartmentId: ApartmentId): Flow<List<Defect>>
    fun observeById(id: DefectId): Flow<Defect?>
    suspend fun getById(id: DefectId): Defect?
    suspend fun save(defect: Defect)                    // Mangel + Medienpfade, nicht den Verlauf
    suspend fun updateStatus(id: DefectId, status: DefectStatus, reportedAt: Instant?)
    suspend fun addCommunication(communication: Communication)
    suspend fun delete(id: DefectId)
    suspend fun addMedia(defectId: DefectId, storedName: String)
    suspend fun removeMedia(defectId: DefectId, storedName: String)
}