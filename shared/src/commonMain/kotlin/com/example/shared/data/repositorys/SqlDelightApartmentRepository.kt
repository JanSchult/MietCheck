package com.example.shared.data.repositorys


import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.data.mapper.toDomain
import com.example.shared.data.mapper.toEntity
import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.LandlordContact
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.LandlordContactId
import com.example.shared.domain.repository.ApartmentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightApartmentRepository(
    private val db: MietCheckDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ApartmentRepository {

    private val q get() = db.apartmentQueries

    override fun observeAll(): Flow<List<Apartment>> =
        q.selectAllApartments().asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: ApartmentId): Apartment? = withContext(dispatcher) {
        q.selectApartmentById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun save(apartment: Apartment) = withContext(dispatcher) {
        q.upsertApartment(apartment.toEntity())
    }

    /** Löscht die Wohnung samt Abrechnungen, Positionen, Fristen, Dokument-Einträgen und Vermieter. */
    override suspend fun delete(id: ApartmentId) = withContext(dispatcher) {
        db.transaction {
            val landlordId = q.selectApartmentById(id.value).executeAsOneOrNull()?.landlordContactId
            db.utilityBillQueries.deleteItemsForApartment(id.value)   // vor den Abrechnungen löschen
            db.utilityBillQueries.deleteBillsForApartment(id.value)
            db.reminderQueries.deleteRemindersForApartment(id.value)
            db.documentQueries.deleteDocumentsForApartment(id.value)
            q.deleteApartment(id.value)
            // Jede Wohnung legt ihren eigenen Vermieter an, deshalb kann er mit gelöscht werden.
            landlordId?.let { q.deleteLandlord(it) }
        }
    }

    override suspend fun getLandlord(id: LandlordContactId): LandlordContact? = withContext(dispatcher) {
        q.selectLandlordById(id.value).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun saveLandlord(contact: LandlordContact) = withContext(dispatcher) {
        q.upsertLandlord(contact.toEntity())
    }
}