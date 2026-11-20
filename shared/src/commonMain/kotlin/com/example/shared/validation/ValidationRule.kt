package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.usecase.formatEuro
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

 const val TOLERANCE_CENTS = 100L           // 1 € Rundungstoleranz

 fun LocalDate.de(): String =
    "${day.toString().padStart(2, '0')}.${month.number.toString().padStart(2, '0')}.$year"

 fun sqm(value: Double): String = (kotlin.math.round(value * 10) / 10).toString().replace('.', ',')

 fun resultText(cents: Long): String =
    if (cents < 0) "Guthaben ${formatEuro(-cents)}" else "Nachzahlung ${formatEuro(cents)}"

 const val NOTHING_TO_DO = "Keine weitere Aktion nötig."

interface ValidationRule {
    val id: String
    fun evaluate(context: ValidationContext): ValidationResult
}

