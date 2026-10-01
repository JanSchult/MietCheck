package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.ValidationSeverity

object LivingSpacePresentRule : ValidationRule {
    override val id = "living_space_present"
    override fun evaluate(current: UtilityBill, previous: UtilityBill?): ValidationResult {
        val sqm = current.livingSpaceSqmAtBilling
        val isPresent = sqm != null && sqm > BigDecimal.ZERO
        return ValidationResult(
            ruleId = id,
            severity = if (isPresent) ValidationSeverity.OK else ValidationSeverity.MISSING,
            whatWasChecked = "Angabe der Wohnfläche in der Abrechnung",
            whatWasFound = if (isPresent) "Wohnfläche: $sqm m² angegeben." else "Keine Wohnfläche erfasst.",
            whyRelevant = "Die Wohnfläche ist Grundlage für viele Verteilerschlüssel und für den Kosten-pro-Quadratmeter-Vergleich.",
            suggestedNextStep = if (isPresent) "Keine weitere Aktion nötig."
            else "Trage die Wohnfläche laut Mietvertrag nach oder prüfe, ob sie auf der Originalabrechnung vermerkt ist."
        )
    }
}