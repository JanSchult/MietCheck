package com.example.shared.viewmodels.state

import com.example.shared.domain.model.BillSummary

data class ChecksState(
    val apartmentId: String? = null,
    val bills: List<BillSummary> = emptyList(),
    val isLoading: Boolean = true
)