package com.example.shared.data.repositorys

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.repository.UtilityBillRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate

class InMemoryUtilityBillRepository : UtilityBillRepository {

    private val state = MutableStateFlow<List<UtilityBill>>(emptyList())

    override suspend fun save(bill: UtilityBill) {
        state.value = state.value.filterNot { it.id == bill.id } + bill
    }

    override suspend fun getById(id: UtilityBillId): UtilityBill? =
        state.value.find { it.id == id }

    override suspend fun getForApartment(apartmentId: ApartmentId): List<UtilityBill> =
        state.value
            .filter { it.apartmentId == apartmentId }
            .sortedByDescending { it.billingPeriodEnd }

    override suspend fun getPreviousYear(
        apartmentId: ApartmentId,
        currentPeriodEnd: LocalDate
    ): UtilityBill? =
        state.value
            .filter { it.apartmentId == apartmentId && it.billingPeriodEnd < currentPeriodEnd }
            .maxByOrNull { it.billingPeriodEnd }

    override suspend fun delete(id: UtilityBillId) {
        state.value = state.value.filterNot { it.id == id }
    }
}