package com.example.shared.validation

import com.example.shared.domain.model.ValidationResult
import com.example.shared.domain.model.enums.ValidationSeverity
import com.example.shared.domain.usecase.formatEuro
import com.example.shared.domain.usecase.formatSignedTenthPercent
import kotlinx.datetime.daysUntil
import kotlin.math.abs

object YearOverYearDeviationRule : ValidationRule {
    override val id = "year_over_year_deviation"

    private const val THRESHOLD_TENTHS = 300L          // 30 %
    private const val MIN_DIFFERENCE_CENTS = 5_000L    // unter 50 € kein Hinweis

    override fun evaluate(context: ValidationContext): ValidationResult {
        val current = context.current
        val previous = context.previous

        // Nur eine direkt vorangehende Abrechnung ist ein fairer Vergleich
        if (previous == null || previous.totalCostsCents <= 0L ||
            previous.billingPeriodEnd.daysUntil(current.billingPeriodEnd) !in 300..430
        ) {
            return ValidationResult(
                ruleId = id,
                severity = ValidationSeverity.OK,
                whatWasChecked = "Veränderung der Gesamtkosten zum Vorjahr",
                whatWasFound = if (previous == null) "Keine Vorjahresabrechnung hinterlegt."
                else "Die zuletzt gespeicherte frühere Abrechnung ist keine direkte Vorjahresabrechnung.",
                whyRelevant = "Ohne Vorjahreswert ist kein Vergleich möglich.",
                suggestedNextStep = "Erfasse die Vorjahresabrechnung, um künftig zu vergleichen."
            )
        }

        val difference = current.totalCostsCents - previous.totalCostsCents
        val tenths = tenthPercent(difference, previous.totalCostsCents)
        val large = abs(tenths) > THRESHOLD_TENTHS && abs(difference) >= MIN_DIFFERENCE_CENTS

        return ValidationResult(
            ruleId = id,
            severity = if (large) ValidationSeverity.DEVIATION else ValidationSeverity.OK,
            whatWasChecked = "Veränderung der Gesamtkosten zum Vorjahr",
            whatWasFound = "Gesamtkosten ${formatEuro(current.totalCostsCents)} (Vorjahr ${formatEuro(previous.totalCostsCents)}), " +
                    "Veränderung ${formatSignedTenthPercent(tenths)}.",
            whyRelevant = "Starke Abweichungen können an gestiegenen Preisen oder Verbräuchen liegen, aber auch an " +
                    "neuen oder veränderten Kostenpositionen.",
            suggestedNextStep = if (large) "Vergleiche die Einzelpositionen mit dem Vorjahr, um die Ursache zu finden. " +
                    "Bei Unklarheit kannst du Einsicht in die Belege erbitten." else NOTHING_TO_DO
        )
    }

    private fun tenthPercent(difference: Long, base: Long): Long {
        val scaled = difference * 1000
        return if (scaled >= 0) (scaled + base / 2) / base else -((-scaled + base / 2) / base)
    }
}