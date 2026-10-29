package com.example.shared.domain.usecase

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.repository.UtilityBillRepository

class SaveUtilityBillUseCase(private val repository: UtilityBillRepository) {
    suspend operator fun invoke(bill: UtilityBill) = repository.save(bill)
}