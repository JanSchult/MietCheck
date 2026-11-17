package com.example.shared.domain.model

import com.example.shared.domain.model.enums.ValidationSeverity

data class ValidationResult(
    val ruleId: String,
    val severity: ValidationSeverity,
    val whatWasChecked: String,
    val whatWasFound: String,
    val whyRelevant: String,
    val suggestedNextStep: String
)