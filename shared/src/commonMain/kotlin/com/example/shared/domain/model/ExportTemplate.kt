package com.example.shared.domain.model

data class ExportTemplate(
    val type: LetterTemplateType,
    val subjectTemplate: String,
    val bodyTemplate: String, // mit Platzhaltern {{apartmentAddress}}, {{billingPeriod}}, etc.
)