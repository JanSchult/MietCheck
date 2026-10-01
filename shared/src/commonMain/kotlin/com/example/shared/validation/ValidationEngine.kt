package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult

class ValidationEngine(private val rules: List<ValidationRule>) {
    fun runAll(current: UtilityBill, previous: UtilityBill?): List<ValidationResult> =
        rules.map { it.evaluate(current, previous) }
}