package com.example.shared.domain.usecase

import com.example.shared.domain.repository.UtilityBillRepository
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.validation.ValidationEngine

class RunValidationUseCase(
    private val billRepository: UtilityBillRepository,
    private val validationEngine: ValidationEngine
) {
    suspend operator fun invoke(billId: UtilityBillId): List<ValidationResult> {
        val bill = billRepository.getById(billId)
            ?: throw IllegalArgumentException("Abrechnung nicht gefunden: $billId")
        val previous = billRepository.getPreviousYear(bill.apartmentId, bill.billingPeriodEnd)
        return validationEngine.runAll(bill, previous)
    }
}