package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.ValidationSeverity

object IssueDateAfterPeriodEndRule : ValidationRule {
    override val id = "issue_date_after_period_end"
    override fun evaluate(current: UtilityBill, previous: UtilityBill?): ValidationResult {
        val issueDate = current.issueDate
        val isPlausible = issueDate == null || !issueDate.isBefore(current.billingPeriodEnd)
        return ValidationResult(
            ruleId = id,
            severity = if (issueDate == null) ValidationSeverity.MISSING
            else if (isPlausible) ValidationSeverity.OK else ValidationSeverity.DEVIATION,
            whatWasChecked = "Reihenfolge von Abrechnungsdatum und Ende des Zeitraums",
            whatWasFound = if (issueDate == null) "Kein Abrechnungsdatum erfasst."
            else "Abrechnungsdatum: $issueDate, Ende des Zeitraums: ${current.billingPeriodEnd}.",
            whyRelevant = "Das Abrechnungsdatum sollte plausiblerweise nach Ende des abgerechneten Zeitraums liegen.",
            suggestedNextStep = if (issueDate == null) "Trage das Abrechnungsdatum nach, falls auf dem Dokument vorhanden."
            else if (isPlausible) "Keine weitere Aktion nötig."
            else "Prüfe das Dokument auf Tippfehler beim Datum."
        )
    }
}