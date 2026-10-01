package com.example.shared.domain.model

import com.example.shared.model.ids.LandlordContactId

data class LandlordContact(
    val id: LandlordContactId,
    val name: String,
    val company: String?,
    val email: String?,
    val phone: String?,
    val postalAddress: Address?
)