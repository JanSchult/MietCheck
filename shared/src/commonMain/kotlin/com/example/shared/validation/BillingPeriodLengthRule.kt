package com.example.shared.validation

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity
import kotlinx.datetime.daysUntil

object BillingPeriodLengthRule : ValidationRule {
    override val id = "billing_period_length"

    override fun evaluate(context: ValidationContext): ValidationResult {
        val bill = context.current
        val days = bill.billingPeriodStart.daysUntil(bill.billingPeriodEnd) + 1
        val tenancy = context.apartment?.tenancyStartDate
        val plausible = days in 350..380
        val explainedByTenancy = !plausible && days in 1..349 && tenancy != null &&
                tenancy > bill.billingPeriodStart && tenancy <= bill.billingPeriodEnd
        val ok = plausible || explainedByTenancy

        return ValidationResult(
            ruleId = id,
            severity = if (ok) ValidationSeverity.OK else ValidationSeverity.DEVIATION,
            whatWasChecked = "Länge des Abrechnungszeitraums",
            whatWasFound = buildString {
                append("Der Zeitraum umfasst $days Tage.")
                if (explainedByTenancy && tenancy != null) {
                    append(" Dein Mietbeginn (${tenancy.de()}) liegt im Zeitraum, das erklärt den kürzeren Zeitraum.")
                }
            },
            whyRelevant = "Abrechnungszeiträume betragen in der Regel etwa zwölf Monate, sofern kein " +
                    "Mieterwechsel oder Sonderfall vorliegt.",
            suggestedNextStep = if (ok) NOTHING_TO_DO
            else "Prüfe, ob ein Ein- oder Auszug oder ein Wechsel des Abrechnungszeitraums die abweichende Länge erklärt."
        )
    }
}