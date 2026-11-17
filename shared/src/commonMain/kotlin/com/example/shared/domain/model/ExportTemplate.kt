package com.example.shared.domain.model

import com.example.shared.domain.model.enums.LetterTemplateType

data class ExportTemplate(
    val type: LetterTemplateType,
    val subjectTemplate: String,
    val bodyTemplate: String, // mit Platzhaltern {{apartmentAddress}}, {{billingPeriod}}, etc.
)