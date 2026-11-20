package com.example.shared.validation

import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity
import com.example.shared.domain.usecase.formatEuro
import kotlin.math.abs

object BalanceMatchesStatementRule : ValidationRule {
    override val id = "balance_matches_statement"

    override fun evaluate(context: ValidationContext): ValidationResult {
        val bill = context.current
        val stated = bill.statedBalanceCents
        val computed = bill.totalCostsCents - bill.advancePaymentsMadeCents

        if (stated == null) {
            return ValidationResult(
                ruleId = id,
                severity = ValidationSeverity.MISSING,
                whatWasChecked = "Ergebnis der Abrechnung (Nachzahlung oder Guthaben)",
                whatWasFound = "Das Ergebnis laut Abrechnung ist nicht erfasst, die Gegenrechnung wurde übersprungen.",
                whyRelevant = "Nur damit lässt sich prüfen, ob Gesamtkosten, Vorauszahlungen und Ergebnis zusammenpassen.",
                suggestedNextStep = "Trage Nachzahlung oder Guthaben aus der Abrechnung im Formular nach."
            )
        }
        val difference = computed - stated
        val ok = abs(difference) < TOLERANCE_CENTS
        return ValidationResult(
            ruleId = id,
            severity = if (ok) ValidationSeverity.OK else ValidationSeverity.DEVIATION,
            whatWasChecked = "Ergebnis der Abrechnung (Nachzahlung oder Guthaben)",
            whatWasFound = if (ok) "Aus Gesamtkosten und Vorauszahlungen ergibt sich ${resultText(computed)}, wie in der Abrechnung."
            else "Aus Gesamtkosten und Vorauszahlungen ergibt sich ${resultText(computed)}, die Abrechnung nennt " +
                    "${resultText(stated)} (Differenz ${formatEuro(abs(difference))}).",
            whyRelevant = "Eine Abweichung kann auf einen Übertragungsfehler bei dir oder auf einen Rechenfehler " +
                    "in der Abrechnung hindeuten.",
            suggestedNextStep = if (ok) NOTHING_TO_DO
            else "Vergleiche deine erfassten Werte mit dem Dokument. Stimmen sie, kannst du den Vermieter um eine Erläuterung bitten."
        )
    }
}