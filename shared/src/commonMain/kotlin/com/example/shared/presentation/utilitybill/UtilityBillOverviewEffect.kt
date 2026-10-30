package com.example.shared.presentation.utilitybill

import com.example.shared.domain.model.LetterTemplateType

sealed interface UtilityBillOverviewEffect {
    data class NavigateToLetterEditor(val templateType: LetterTemplateType, val billId: String) :
        UtilityBillOverviewEffect
    data class ShowError(val message: String) : UtilityBillOverviewEffect
}