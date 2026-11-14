package com.example.shared.viewmodels.effects

sealed interface ChecksEffect {
    data class NavigateToForm(val apartmentId: String) : ChecksEffect
    data class NavigateToBill(val billId: String) : ChecksEffect
}