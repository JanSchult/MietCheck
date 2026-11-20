package com.example.shared.domain.usecase

import com.example.shared.domain.repository.UtilityBillRepository
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.repository.ApartmentRepository
import com.example.shared.validation.ValidationContext
import com.example.shared.validation.ValidationEngine

class RunValidationUseCase(
    private val billRepository: UtilityBillRepository,
    private val apartmentRepository: ApartmentRepository,
    private val validationEngine: ValidationEngine
) {
    suspend operator fun invoke(billId: UtilityBillId): List<ValidationResult> {
        val current = billRepository.getById(billId)
            ?: throw IllegalArgumentException("Abrechnung nicht gefunden: ${billId.value}")
        val previous = billRepository.getPreviousYear(current.apartmentId, current.billingPeriodEnd)
        val apartment = apartmentRepository.getById(current.apartmentId)
        return validationEngine.runAll(ValidationContext(current, previous, apartment))
    }
}