package com.example.shared.domain.usecase

import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationSeverity
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.repository.DocumentRepository
import com.example.shared.domain.repository.UtilityBillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveRecentDocumentsUseCase(private val repository: DocumentRepository) {
    operator fun invoke(apartmentId: ApartmentId, limit: Int = 5): Flow<List<DocumentRecord>> =
        repository.observeForApartment(apartmentId).map { it.take(limit) }
}

data class OpenCheck(val bill: UtilityBill, val findingsCount: Int)

/** Abrechnungen, bei denen mindestens eine Prüfung nicht "OK" ist. */
class GetOpenChecksUseCase(
    private val billRepository: UtilityBillRepository,
    private val runValidation: RunValidationUseCase
) {
    suspend operator fun invoke(apartmentId: ApartmentId): List<OpenCheck> =
        billRepository.getForApartment(apartmentId).mapNotNull { bill ->
            val findings = runValidation(bill.id).count { it.severity != ValidationSeverity.OK }
            if (findings > 0) OpenCheck(bill, findings) else null
        }
}