package com.example.shared.viewmodels.state

import com.example.shared.domain.model.BillFormField
import com.example.shared.domain.model.CostItemInput
import com.example.shared.domain.usecase.parseEuroToCents

data class UtilityBillFormState(
    val apartmentId: String? = null,
    val periodStart: String = "",
    val periodEnd: String = "",
    val issueDate: String = "",
    val livingSpace: String = "",
    val occupants: String = "",
    val advancePayments: String = "",
    val totalCosts: String = "",
    val items: List<CostItemInput> = emptyList(),
    val fieldErrors: Map<BillFormField, String> = emptyMap(),
    val itemErrors: Map<Long, String> = emptyMap(),
    val isSaving: Boolean = false
) {
    // Live-Werte für die Zusammenfassung (alles in Cent, kein Double)
    val itemsSumCents: Long get() = items.sumOf { parseEuroToCents(it.amount) ?: 0L }
    val totalCents: Long? get() = parseEuroToCents(totalCosts)
    val differenceCents: Long? get() = totalCents?.let { it - itemsSumCents }
    val balanceCents: Long?
        get() {
            val total = totalCents
            val advance = parseEuroToCents(advancePayments)
            return if (total != null && advance != null) total - advance else null
        }
}