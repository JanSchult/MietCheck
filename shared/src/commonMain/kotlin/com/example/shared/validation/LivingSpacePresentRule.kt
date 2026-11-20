package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity

object LivingSpacePresentRule : ValidationRule {
    override val id = "living_space_present"

    override fun evaluate(context: ValidationContext): ValidationResult {
        val sqm = context.current.livingSpaceSqmAtBilling
        val present = sqm != null && sqm > 0.0
        return ValidationResult(
            ruleId = id,
            severity = if (present) ValidationSeverity.OK else ValidationSeverity.MISSING,
            whatWasChecked = "Angabe der Wohnfläche in der Abrechnung",
            whatWasFound = if (present && sqm != null) "Wohnfläche: ${sqm(sqm)} m² angegeben." else "Keine Wohnfläche erfasst.",
            whyRelevant = "Die Wohnfläche ist Grundlage vieler Verteilerschlüssel und des Vergleichs der Kosten pro Quadratmeter.",
            suggestedNextStep = if (present) NOTHING_TO_DO
            else "Trage die Wohnfläche laut Abrechnung nach oder prüfe, ob sie dort vermerkt ist."
        )
    }
}