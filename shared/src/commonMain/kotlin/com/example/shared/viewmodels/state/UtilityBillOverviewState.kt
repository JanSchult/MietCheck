package com.example.shared.viewmodels.state

import com.example.shared.domain.model.BillOverview
import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ValidationResult

data class UtilityBillOverviewState(
    val overview: BillOverview? = null,
    val validationResults: List<ValidationResult> = emptyList(),
    val isLoading: Boolean = false,
    val isValidating: Boolean = false,
    val hasValidated: Boolean = false
)