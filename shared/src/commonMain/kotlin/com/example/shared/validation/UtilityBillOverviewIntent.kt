package com.example.shared.validation

import com.example.shared.domain.model.LetterTemplateType
import com.example.shared.domain.model.ids.UtilityBillId

sealed interface UtilityBillOverviewIntent {
    data class LoadBill(val billId: UtilityBillId) : UtilityBillOverviewIntent
    data object RunValidation : UtilityBillOverviewIntent
    data class StartLetterDraft(val templateType: LetterTemplateType) : UtilityBillOverviewIntent
}
