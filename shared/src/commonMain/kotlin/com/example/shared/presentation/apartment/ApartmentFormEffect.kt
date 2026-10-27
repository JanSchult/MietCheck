package com.example.shared.presentation.apartment

sealed interface ApartmentFormEffect {
    data class Saved(val apartmentId: String) : ApartmentFormEffect
    data class ShowError(val message: String) : ApartmentFormEffect
}