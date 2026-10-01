package com.example.shared.validation

sealed interface UtilityBillOverviewEffect {
    data class NavigateToLetterEditor(val draftId: String) : UtilityBillOverviewEffect
    data class ShowError(val message: String) : UtilityBillOverviewEffect
}