package com.example.shared.viewmodels.state

import com.example.shared.domain.model.ApartmentField

data class ApartmentFormState(
    val label: String = "",
    val street: String = "",
    val houseNumber: String = "",
    val postalCode: String = "",
    val city: String = "",
    val livingSpace: String = "",
    val occupants: String = "1",
    val tenancyStart: String = "",          // yyyy-MM-dd, DatePicker folgt in der UI
    val utilityAdvance: String = "",
    val heatingAdvance: String = "",
    val landlordName: String = "",
    val landlordEmail: String = "",
    val landlordPhone: String = "",
    val fieldErrors: Map<ApartmentField, String> = emptyMap(),
    val isSaving: Boolean = false
)