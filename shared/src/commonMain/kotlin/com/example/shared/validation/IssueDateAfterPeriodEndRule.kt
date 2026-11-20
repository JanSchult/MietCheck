package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity

object IssueDateAfterPeriodEndRule : ValidationRule {
    override val id = "issue_date_after_period_end"

    override fun evaluate(context: ValidationContext): ValidationResult {
        val bill = context.current
        val issueDate = bill.issueDate
        val plausible = issueDate == null || issueDate >= bill.billingPeriodEnd
        return ValidationResult(
            ruleId = id,
            severity = when {
                issueDate == null -> ValidationSeverity.MISSING
                plausible -> ValidationSeverity.OK
                else -> ValidationSeverity.DEVIATION
            },
            whatWasChecked = "Reihenfolge von Abrechnungsdatum und Ende des Zeitraums",
            whatWasFound = if (issueDate == null) "Kein Abrechnungsdatum erfasst."
            else "Abrechnungsdatum: ${issueDate.de()}, Ende des Zeitraums: ${bill.billingPeriodEnd.de()}.",
            whyRelevant = "Das Abrechnungsdatum sollte plausiblerweise nach Ende des abgerechneten Zeitraums liegen.",
            suggestedNextStep = when {
                issueDate == null -> "Trage das Abrechnungsdatum nach, falls es auf dem Dokument steht."
                plausible -> NOTHING_TO_DO
                else -> "Prüfe das Dokument auf einen Tippfehler beim Datum."
            }
        )
    }
}