package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.ValidationSeverity
import com.example.shared.domain.usecase.formatEuro
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlin.math.abs

object AdvancePaymentMultiplicationRule : ValidationRule {
    override val id = "advance_payment_multiplication"

    override fun evaluate(current: UtilityBill, previous: UtilityBill?): ValidationResult {
        // Ende ist inklusive, deshalb +1 Tag, sonst fehlt beim 31.12. ein Monat
        val months = current.billingPeriodStart
            .monthsUntil(current.billingPeriodEnd.plus(1, DateTimeUnit.DAY))
            .coerceAtLeast(1)

        val monthlyEstimate = current.advancePaymentsMadeCents / months
        val expectedTotal = monthlyEstimate * months
        val deviation = abs(current.advancePaymentsMadeCents - expectedTotal)
        val isPlausible = deviation < 100 // Toleranz 1 Euro für Rundung

        return ValidationResult(
            ruleId = id,
            severity = if (isPlausible) ValidationSeverity.OK else ValidationSeverity.REVIEW_RECOMMENDED,
            whatWasChecked = "Rechnerische Konsistenz der geleisteten Vorauszahlungen über $months Monate",
            whatWasFound = "Erfasste Vorauszahlungen: ${formatEuro(current.advancePaymentsMadeCents)}, " +
                    "rechnerisch erwartet bei gleichbleibender Monatsrate: ${formatEuro(expectedTotal)}.",
            whyRelevant = "Größere Abweichungen können auf geänderte Vorauszahlungen während des Zeitraums " +
                    "oder auf einen Erfassungsfehler hindeuten.",
            suggestedNextStep = if (isPlausible) "Keine weitere Aktion nötig."
            else "Vergleiche mit deinen tatsächlichen Kontoauszügen oder dem Mietvertrag auf Änderungen der Vorauszahlung."
        )
    }
}