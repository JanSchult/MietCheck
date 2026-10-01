package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.ValidationSeverity

object DuplicateCostItemsRule : ValidationRule {
    override val id = "duplicate_cost_items"
    override fun evaluate(current: UtilityBill, previous: UtilityBill?): ValidationResult {
        val duplicates = current.items
            .groupBy { it.category }
            .filter { it.value.size > 1 }
        val isPlausible = duplicates.isEmpty()
        return ValidationResult(
            ruleId = id,
            severity = if (isPlausible) ValidationSeverity.OK else ValidationSeverity.REVIEW_RECOMMENDED,
            whatWasChecked = "Mehrfach vorkommende Kostenkategorien in derselben Abrechnung",
            whatWasFound = if (isPlausible) "Keine doppelten Kategorien gefunden."
            else "Mehrfach erfasst: ${duplicates.keys.joinToString { it.name }}.",
            whyRelevant = "Doppelte Positionen können ein Erfassungsfehler sein, aber auch bewusst getrennte Teilkosten derselben Kategorie (z. B. zwei Zähler).",
            suggestedNextStep = if (isPlausible) "Keine weitere Aktion nötig."
            else "Prüfe auf der Originalabrechnung, ob es sich um separate, begründete Teilpositionen handelt."
        )
    }
}