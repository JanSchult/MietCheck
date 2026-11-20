package com.example.shared.validation

import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity

object RequiredFieldsRule : ValidationRule {
    override val id = "required_fields"

    override fun evaluate(context: ValidationContext): ValidationResult {
        val bill = context.current
        val missing = buildList {
            if (bill.items.isEmpty()) add("Kostenpositionen")
            if (bill.totalCostsCents <= 0L) add("Gesamtkosten")
            if (bill.advancePaymentsMadeCents <= 0L) add("geleistete Vorauszahlungen")
        }
        return ValidationResult(
            ruleId = id,
            severity = if (missing.isEmpty()) ValidationSeverity.OK else ValidationSeverity.MISSING,
            whatWasChecked = "Vollständigkeit der wichtigsten Angaben",
            whatWasFound = if (missing.isEmpty()) "Positionen, Gesamtkosten und Vorauszahlungen sind erfasst."
            else "Es fehlen: ${missing.joinToString(", ")}.",
            whyRelevant = "Ohne diese Angaben lässt sich die Abrechnung nicht sinnvoll prüfen.",
            suggestedNextStep = if (missing.isEmpty()) NOTHING_TO_DO
            else "Ergänze die fehlenden Angaben aus der Originalabrechnung. Fehlen sie dort, kannst du den Vermieter darum bitten."
        )
    }
}