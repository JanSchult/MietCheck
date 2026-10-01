package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult

interface ValidationRule {
    val id: String
    fun evaluate(current: UtilityBill, previous: UtilityBill?): ValidationResult
}