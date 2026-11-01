package com.example.shared.data.repositorys


import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.data.mapper.toDomain
import com.example.shared.data.mapper.toEntity
import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.repository.UtilityBillRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class SqlDelightUtilityBillRepository(
    private val db: MietCheckDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : UtilityBillRepository {

    private val q get() = db.utilityBillQueries

    override suspend fun save(bill: UtilityBill) = withContext(dispatcher) {
        db.transaction {
            q.upsertBill(bill.toEntity())
            q.deleteItemsForBill(bill.id.value)
            bill.items.forEachIndexed { index, item ->
                q.insertItem(item.toEntity(bill.id.value, index))
            }
        }
    }

    override suspend fun getById(id: UtilityBillId): UtilityBill? = withContext(dispatcher) {
        val row = q.selectBillById(id.value).executeAsOneOrNull() ?: return@withContext null
        row.toDomain(q.selectItemsForBill(id.value).executeAsList())
    }

    override suspend fun getForApartment(apartmentId: ApartmentId): List<UtilityBill> = withContext(dispatcher) {
        val items = q.selectItemsForApartment(apartmentId.value).executeAsList().groupBy { it.billId }
        q.selectBillsForApartment(apartmentId.value).executeAsList()
            .map { it.toDomain(items[it.id].orEmpty()) }
    }

    override fun observeForApartment(apartmentId: ApartmentId): Flow<List<UtilityBill>> =
        combine(
            q.selectBillsForApartment(apartmentId.value).asFlow().mapToList(dispatcher),
            q.selectItemsForApartment(apartmentId.value).asFlow().mapToList(dispatcher)
        ) { bills, items ->
            val byBill = items.groupBy { it.billId }
            bills.map { it.toDomain(byBill[it.id].orEmpty()) }
        }

    override suspend fun getPreviousYear(
        apartmentId: ApartmentId,
        currentPeriodEnd: LocalDate
    ): UtilityBill? = withContext(dispatcher) {
        val row = q.selectPreviousBill(apartmentId.value, currentPeriodEnd.toString())
            .executeAsOneOrNull() ?: return@withContext null
        row.toDomain(q.selectItemsForBill(row.id).executeAsList())
    }

    override suspend fun delete(id: UtilityBillId) = withContext(dispatcher) {
        db.transaction {
            q.deleteItemsForBill(id.value)
            q.deleteBill(id.value)
        }
    }
}