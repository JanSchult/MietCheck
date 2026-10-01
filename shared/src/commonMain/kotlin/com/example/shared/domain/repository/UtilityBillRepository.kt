package com.example.shared.domain.repository

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.UtilityBillId
import kotlinx.datetime.LocalDate

interface UtilityBillRepository {
    suspend fun save(bill: UtilityBill): Unit
    suspend fun getById(id: UtilityBillId): UtilityBill?
    suspend fun getForApartment(apartmentId: ApartmentId): List<UtilityBill>
    suspend fun getPreviousYear(apartmentId: ApartmentId, currentPeriodEnd: LocalDate): UtilityBill?
    suspend fun delete(id: UtilityBillId): Unit
}