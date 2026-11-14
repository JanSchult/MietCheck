package com.example.shared.viewmodels.intent

import com.example.shared.domain.model.LetterTemplateType

sealed interface UtilityBillOverviewIntent {
    data class LoadBill(val billId: String) : UtilityBillOverviewIntent
    data object RunValidation : UtilityBillOverviewIntent
    data class StartLetterDraft(val templateType: LetterTemplateType) : UtilityBillOverviewIntent
}