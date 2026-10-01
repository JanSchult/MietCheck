package com.example.shared.domain.model

data class ValidationResult(
    val ruleId: String,
    val severity: ValidationSeverity,
    val whatWasChecked: String,
    val whatWasFound: String,
    val whyRelevant: String,
    val suggestedNextStep: String
)