package com.example.shared.validation

import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity
import com.example.shared.domain.usecase.formatEuro
import kotlin.math.abs

object ItemsSumMatchesTotalRule : ValidationRule {
    override val id = "items_sum_matches_total"

    override fun evaluate(context: ValidationContext): ValidationResult {
        val bill = context.current
        if (bill.items.isEmpty()) {
            return ValidationResult(
                ruleId = id,
                severity = ValidationSeverity.OK,
                whatWasChecked = "Summe der Einzelpositionen gegenüber der Gesamtsumme",
                whatWasFound = "Es sind keine Kostenpositionen erfasst, ein Vergleich ist nicht möglich.",
                whyRelevant = "Ohne Positionen lässt sich die Gesamtsumme nicht nachrechnen.",
                suggestedNextStep = "Siehe Hinweis zu den fehlenden Angaben."
            )
        }
        val sum = bill.items.sumOf { it.amountCents }
        val difference = bill.totalCostsCents - sum
        val ok = abs(difference) < TOLERANCE_CENTS
        return ValidationResult(
            ruleId = id,
            severity = if (ok) ValidationSeverity.OK else ValidationSeverity.REVIEW_RECOMMENDED,
            whatWasChecked = "Summe der Einzelpositionen gegenüber der Gesamtsumme",
            whatWasFound = if (ok) "Die Positionen ergeben ${formatEuro(sum)}, passend zur Gesamtsumme."
            else "Die Positionen ergeben ${formatEuro(sum)}, die Gesamtsumme ist ${formatEuro(bill.totalCostsCents)} " +
                    "(Differenz ${formatEuro(abs(difference))}).",
            whyRelevant = "Eine Differenz kann auf eine fehlende Position, einen Erfassungsfehler oder eine " +
                    "nicht aufgeschlüsselte Sammelposition hindeuten.",
            suggestedNextStep = if (ok) NOTHING_TO_DO
            else "Prüfe die Originalabrechnung auf Positionen, die du noch nicht erfasst hast, und auf Zahlendreher."
        )
    }
}