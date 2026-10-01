package com.example.shared.viewmodels.state

import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult

data class UtilityBillOverviewState(
    val bill: UtilityBill? = null,
    val validationResults: List<ValidationResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)