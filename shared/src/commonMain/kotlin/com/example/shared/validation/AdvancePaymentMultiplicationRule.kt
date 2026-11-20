package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity
import com.example.shared.domain.usecase.formatEuro
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlin.math.abs

object AdvancePaymentMultiplicationRule : ValidationRule {
    override val id = "advance_payment_multiplication"

    override fun evaluate(context: ValidationContext): ValidationResult {
        val bill = context.current
        val apartment = context.apartment
        val monthly = if (apartment != null) apartment.monthlyUtilityAdvanceCents + apartment.monthlyHeatingAdvanceCents else 0L

        if (apartment == null || monthly <= 0L) {
            return ValidationResult(
                ruleId = id,
                severity = ValidationSeverity.MISSING,
                whatWasChecked = "Geleistete Vorauszahlungen gegenüber deinen Wohnungsdaten",
                whatWasFound = "In deinen Wohnungsdaten ist keine monatliche Vorauszahlung hinterlegt, ein Vergleich ist nicht möglich.",
                whyRelevant = "Nur mit der monatlichen Rate lässt sich nachrechnen, ob die Summe der Vorauszahlungen stimmt.",
                suggestedNextStep = "Trage die Nebenkosten- und Heizkosten-Vorauszahlung in den Wohnungsdaten ein."
            )
        }

        // Beginnt das Mietverhältnis im Zeitraum, zählen nur die Monate ab Mietbeginn
        val start = if (apartment.tenancyStartDate > bill.billingPeriodStart) apartment.tenancyStartDate else bill.billingPeriodStart
        val months = start.monthsUntil(bill.billingPeriodEnd.plus(1, DateTimeUnit.DAY)).coerceAtLeast(1)
        val expected = monthly * months
        val difference = bill.advancePaymentsMadeCents - expected
        val ok = abs(difference) < TOLERANCE_CENTS

        return ValidationResult(
            ruleId = id,
            severity = if (ok) ValidationSeverity.OK else ValidationSeverity.REVIEW_RECOMMENDED,
            whatWasChecked = "Geleistete Vorauszahlungen gegenüber deinen Wohnungsdaten",
            whatWasFound = "In der Abrechnung: ${formatEuro(bill.advancePaymentsMadeCents)}. Nach deinen Wohnungsdaten: " +
                    "${formatEuro(monthly)} × $months Monate = ${formatEuro(expected)}." +
                    if (ok) "" else " Differenz: ${formatEuro(abs(difference))}.",
            whyRelevant = "Eine Abweichung kann an geänderten Vorauszahlungen, einem anderen Zeitraum oder einem " +
                    "Erfassungsfehler liegen. Die Heizkosten-Vorauszahlung zählt mit.",
            suggestedNextStep = if (ok) NOTHING_TO_DO
            else "Vergleiche mit deinen Kontoauszügen und dem Mietvertrag, ob sich die Vorauszahlung im Zeitraum geändert hat."
        )
    }
}
