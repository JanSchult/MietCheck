package com.example.shared.domain.model

data class LetterDraft(
    val templateType: LetterTemplateType,
    val subject: String,
    val body: String
)