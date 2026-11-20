package com.example.shared.validation

import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity
import kotlin.math.abs

object ApartmentDataMatchesRule : ValidationRule {
    override val id = "apartment_data_matches"

    override fun evaluate(context: ValidationContext): ValidationResult {
        val bill = context.current
        val apartment = context.apartment
        val billSqm = bill.livingSpaceSqmAtBilling
        val billOccupants = bill.occupantsAtBilling

        val findings = mutableListOf<String>()
        if (apartment != null && billSqm != null && billSqm > 0.0 && abs(billSqm - apartment.livingSpaceSqm) > 0.5) {
            findings += "Wohnfläche: ${sqm(billSqm)} m² in der Abrechnung, ${sqm(apartment.livingSpaceSqm)} m² in deinen Wohnungsdaten."
        }
        if (apartment != null && billOccupants != null && billOccupants != apartment.numberOfOccupants) {
            findings += "Bewohner: $billOccupants in der Abrechnung, ${apartment.numberOfOccupants} in deinen Wohnungsdaten."
        }
        val compared = apartment != null && (billSqm != null || billOccupants != null)

        return ValidationResult(
            ruleId = id,
            severity = if (findings.isEmpty()) ValidationSeverity.OK else ValidationSeverity.REVIEW_RECOMMENDED,
            whatWasChecked = "Wohnfläche und Bewohnerzahl gegenüber deinen Wohnungsdaten",
            whatWasFound = when {
                findings.isNotEmpty() -> findings.joinToString(" ")
                compared -> "Wohnfläche und Bewohnerzahl stimmen mit deinen Wohnungsdaten überein."
                else -> "Keine Angaben zum Vergleich erfasst."
            },
            whyRelevant = "Wohnfläche und Personenzahl bestimmen bei vielen Verteilerschlüsseln deinen Kostenanteil. " +
                    "Abweichungen können berechtigte Gründe haben, etwa einen Wechsel der Bewohnerzahl, oder auf einen Fehler hindeuten.",
            suggestedNextStep = if (findings.isEmpty()) NOTHING_TO_DO
            else "Vergleiche mit deinem Mietvertrag. Bei Unklarheit kannst du den Vermieter um eine Erläuterung bitten."
        )
    }
}