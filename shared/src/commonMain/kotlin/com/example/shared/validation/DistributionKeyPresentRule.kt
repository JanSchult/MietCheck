package com.example.shared.validation

import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity

object DistributionKeyPresentRule : ValidationRule {
    override val id = "distribution_key_present"

    override fun evaluate(context: ValidationContext): ValidationResult {
        val bill = context.current
        val noItems = bill.items.isEmpty()
        val ok = noItems || bill.distributionKeyNoted
        return ValidationResult(
            ruleId = id,
            severity = if (ok) ValidationSeverity.OK else ValidationSeverity.MISSING,
            whatWasChecked = "Angabe eines Verteilerschlüssels",
            whatWasFound = when {
                noItems -> "Es sind keine Kostenpositionen erfasst."
                bill.distributionKeyNoted -> "Für mindestens eine Position ist ein Verteilerschlüssel erfasst."
                else -> "Für keine Position ist ein Verteilerschlüssel erfasst."
            },
            whyRelevant = "Der Verteilerschlüssel zeigt, nach welchem Maßstab die Kosten auf die Wohnungen verteilt wurden.",
            suggestedNextStep = if (ok) NOTHING_TO_DO
            else "Prüfe, ob die Abrechnung den Schlüssel nennt. Falls nicht, kannst du den Vermieter danach fragen."
        )
    }
}