package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.ValidationSeverity

object ItemsSumMatchesTotalRule : ValidationRule {
    override val id = "items_sum_matches_total"
    override fun evaluate(current: UtilityBill, previous: UtilityBill?): ValidationResult {
        val diff = BillingCalculator.itemsVsTotalDifferenceCents(current)
        val isPlausible = kotlin.math.abs(diff) < 100 // Toleranz 1 Euro
        return ValidationResult(
            ruleId = id,
            severity = if (isPlausible) ValidationSeverity.OK else ValidationSeverity.REVIEW_RECOMMENDED,
            whatWasChecked = "Übereinstimmung zwischen Summe der Einzelpositionen und ausgewiesener Gesamtsumme",
            whatWasFound = if (isPlausible) "Summe der Einzelpositionen entspricht der Gesamtsumme."
            else "Differenz von ${diff.toEuroString()} zwischen Einzelpositionen und Gesamtsumme.",
            whyRelevant = "Eine Abweichung kann auf eine fehlende Position, einen Erfassungsfehler oder eine nicht aufgeschlüsselte Sammelposition hindeuten.",
            suggestedNextStep = if (isPlausible) "Keine weitere Aktion nötig."
            else "Prüfe die Originalabrechnung auf zusätzliche, nicht erfasste Positionen oder Sammelposten."
        )
    }
}