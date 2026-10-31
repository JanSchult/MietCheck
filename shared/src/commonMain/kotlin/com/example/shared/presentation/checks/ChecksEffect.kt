package com.example.shared.presentation.checks

sealed interface ChecksEffect {
    data class NavigateToForm(val apartmentId: String) : ChecksEffect
    data class NavigateToBill(val billId: String) : ChecksEffect
}