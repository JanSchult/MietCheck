package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity
import com.example.shared.domain.usecase.formatEuro

object DuplicateCostItemsRule : ValidationRule {
    override val id = "duplicate_cost_items"

    override fun evaluate(context: ValidationContext): ValidationResult {
        // Gleiche Kategorie und gleicher Betrag: typisches Zeichen für eine doppelt erfasste Zeile
        val duplicates = context.current.items
            .groupBy { it.category to it.amountCents }
            .filter { it.value.size > 1 && it.key.second > 0L }
        val ok = duplicates.isEmpty()
        return ValidationResult(
            ruleId = id,
            severity = if (ok) ValidationSeverity.OK else ValidationSeverity.REVIEW_RECOMMENDED,
            whatWasChecked = "Doppelt vorkommende Kostenpositionen",
            whatWasFound = if (ok) "Keine Positionen mit gleicher Kategorie und gleichem Betrag gefunden."
            else duplicates.values.joinToString("; ") { group ->
                val first = group.first()
                "${first.label.ifBlank { first.category.name }} (${formatEuro(first.amountCents)}, ${group.size}×)"
            },
            whyRelevant = "Doppelte Zeilen können ein Erfassungsfehler sein, aber auch bewusst getrennte " +
                    "Teilkosten, zum Beispiel bei zwei Zählern.",
            suggestedNextStep = if (ok) NOTHING_TO_DO
            else "Prüfe auf der Originalabrechnung, ob es sich um getrennte, begründete Positionen handelt."
        )
    }
}
