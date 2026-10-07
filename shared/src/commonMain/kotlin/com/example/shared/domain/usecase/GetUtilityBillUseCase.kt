package com.example.shared.domain.usecase

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.repository.UtilityBillRepository

class GetUtilityBillUseCase(
    private val repository: UtilityBillRepository
) {
    suspend operator fun invoke(id: UtilityBillId): UtilityBill? =
        repository.getById(id)
}