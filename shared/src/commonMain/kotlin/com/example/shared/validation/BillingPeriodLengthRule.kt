package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.ValidationSeverity

object BillingPeriodLengthRule : ValidationRule {
    override val id = "billing_period_length"
    override fun evaluate(current: UtilityBill, previous: UtilityBill?): ValidationResult {
        val days = ChronoUnit.DAYS.between(current.billingPeriodStart, current.billingPeriodEnd) + 1
        val isPlausible = days in 350..380 // ~12 Monate mit Toleranz
        return ValidationResult(
            ruleId = id,
            severity = if (isPlausible) ValidationSeverity.OK else ValidationSeverity.DEVIATION,
            whatWasChecked = "Länge des Abrechnungszeitraums",
            whatWasFound = "Der Zeitraum umfasst $days Tage.",
            whyRelevant = "Abrechnungszeiträume sollten in der Regel etwa zwölf Monate betragen, sofern kein Mieterwechsel oder Sonderfall vorliegt.",
            suggestedNextStep = if (isPlausible) "Keine weitere Aktion nötig."
            else "Prüfe, ob ein Ein- oder Auszug während des Zeitraums stattgefunden hat, der die abweichende Länge erklärt."
        )
    }
}