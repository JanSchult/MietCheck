package com.example.shared.viewmodels.effects

sealed interface ApartmentFormEffect {
    data class Saved(val apartmentId: String) : ApartmentFormEffect
    data class ShowError(val message: String) : ApartmentFormEffect
}