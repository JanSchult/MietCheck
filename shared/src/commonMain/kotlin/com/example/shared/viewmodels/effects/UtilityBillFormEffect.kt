package com.example.shared.viewmodels.effects

sealed interface UtilityBillFormEffect {
    data class Saved(val billId: String) : UtilityBillFormEffect
    data class ShowError(val message: String) : UtilityBillFormEffect
}