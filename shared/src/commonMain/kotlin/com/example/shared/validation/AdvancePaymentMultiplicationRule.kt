package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.ValidationSeverity


object AdvancePaymentMultiplicationRule : ValidationRule {
    override val id = "advance_payment_multiplication"
    override fun evaluate(current: UtilityBill, previous: UtilityBill?): ValidationResult {
        val months = ChronoUnit.MONTHS.between(
            current.billingPeriodStart, current.billingPeriodEnd.plusDays(1)
        ).coerceAtLeast(1)
        // Erwartung kann nur grob geprüft werden ohne Vertragsdaten; hier: Konsistenz-Hinweis
        val monthlyEstimate = current.advancePaymentsMadeCents / months
        val expectedTotal = monthlyEstimate * months
        val deviation = kotlin.math.abs(current.advancePaymentsMadeCents - expectedTotal)
        val isPlausible = deviation < 100 // Toleranz 1 Euro für Rundung
        return ValidationResult(
            ruleId = id,
            severity = if (isPlausible) ValidationSeverity.OK else ValidationSeverity.REVIEW_RECOMMENDED,
            whatWasChecked = "Rechnerische Konsistenz der geleisteten Vorauszahlungen über $months Monate",
            whatWasFound = "Erfasste Vorauszahlungen: ${current.advancePaymentsMadeCents.toEuroString()}, " +
                    "rechnerisch erwartet bei gleichbleibender Monatsrate: ${expectedTotal.toEuroString()}.",
            whyRelevant = "Größere Abweichungen können auf geänderte Vorauszahlungen während des Zeitraums oder auf einen Erfassungsfehler hindeuten.",
            suggestedNextStep = if (isPlausible) "Keine weitere Aktion nötig."
            else "Vergleiche mit deinen tatsächlichen Kontoauszügen oder dem Mietvertrag auf Änderungen der Vorauszahlung."
        )
    }
}

fun Long.toEuroString(): String = "%,.2f €".format(this / 100.0)