package com.example.shared.presentation.apartment

import com.example.shared.domain.model.ApartmentFormField

sealed interface ApartmentFormIntent {
    data class FieldChanged(val field: ApartmentFormField, val value: String) : ApartmentFormIntent
    data object Save : ApartmentFormIntent
}