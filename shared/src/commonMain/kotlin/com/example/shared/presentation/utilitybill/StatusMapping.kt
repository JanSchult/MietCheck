package com.example.shared.presentation.utilitybill

import com.example.shared.domain.model.ItemStatus
import com.example.shared.domain.model.LetterTemplateType
import com.example.shared.domain.model.StatusKind
import com.example.shared.domain.model.ValidationSeverity

fun ValidationSeverity.kind(): StatusKind = when (this) {
    ValidationSeverity.OK -> StatusKind.OK
    ValidationSeverity.REVIEW_RECOMMENDED, ValidationSeverity.MISSING -> StatusKind.NOTE
    ValidationSeverity.DEVIATION, ValidationSeverity.LEGAL_REVIEW_RECOMMENDED -> StatusKind.ATTENTION
}

fun ValidationSeverity.label(): String = when (this) {
    ValidationSeverity.OK -> "Keine Auffälligkeit erkannt"
    ValidationSeverity.REVIEW_RECOMMENDED -> "Manuelle Prüfung empfohlen"
    ValidationSeverity.MISSING -> "Angabe fehlt"
    ValidationSeverity.DEVIATION -> "Abweichung erkannt"
    ValidationSeverity.LEGAL_REVIEW_RECOMMENDED -> "Rechtliche Prüfung erforderlich"
}

fun ItemStatus.kind(): StatusKind = when (this) {
    ItemStatus.NO_FINDING -> StatusKind.OK
    ItemStatus.MANUAL_REVIEW, ItemStatus.MISSING_INFO -> StatusKind.NOTE
    ItemStatus.YOY_DEVIATION, ItemStatus.LEGAL_REVIEW -> StatusKind.ATTENTION
}

fun ItemStatus.label(): String = when (this) {
    ItemStatus.NO_FINDING -> "Keine Auffälligkeit erkannt"
    ItemStatus.MANUAL_REVIEW -> "Manuelle Prüfung empfohlen"
    ItemStatus.MISSING_INFO -> "Angabe fehlt"
    ItemStatus.YOY_DEVIATION -> "Wert weicht stark vom Vorjahr ab"
    ItemStatus.LEGAL_REVIEW -> "Rechtliche Prüfung erforderlich"
}

fun LetterTemplateType.displayName(): String = when (this) {
    LetterTemplateType.REQUEST_EXPLANATION -> "Erläuterung einer Position erbitten"
    LetterTemplateType.REQUEST_RECEIPT_ACCESS -> "Einsicht in die Belege erbitten"
    LetterTemplateType.NOTE_CALCULATION_DISCREPANCY -> "Auf eine rechnerische Unstimmigkeit hinweisen"
    LetterTemplateType.REQUEST_DISTRIBUTION_KEY -> "Zum Verteilerschlüssel nachfragen"
    LetterTemplateType.REQUEST_CORRECTION_MISSING_DATA -> "Fehlende Angaben nachfordern"
    LetterTemplateType.GENERAL_INQUIRY -> "Allgemeine Rückfrage"
}

/** Welche Vorlage passt zu welchem Prüfergebnis? */
fun suggestedTemplateFor(ruleId: String): LetterTemplateType = when (ruleId) {
    "items_sum_matches_total",
    "balance_calculation_basis",
    "advance_payment_multiplication" -> LetterTemplateType.NOTE_CALCULATION_DISCREPANCY
    "living_space_present" -> LetterTemplateType.REQUEST_CORRECTION_MISSING_DATA
    "duplicate_cost_items" -> LetterTemplateType.REQUEST_EXPLANATION
    "year_over_year_deviation" -> LetterTemplateType.REQUEST_RECEIPT_ACCESS
    else -> LetterTemplateType.GENERAL_INQUIRY
}