package com.example.shared.validation

import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.UtilityBill

data class ValidationContext(
    val current: UtilityBill,
    val previous: UtilityBill?,
    val apartment: Apartment?
)