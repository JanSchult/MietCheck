package com.example.shared.domain.model

import com.example.shared.domain.model.enums.LetterTemplateType

data class LetterDraft(
    val templateType: LetterTemplateType,
    val subject: String,
    val body: String
)