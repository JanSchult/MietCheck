package com.example.shared.viewmodels.state

import com.example.shared.domain.model.Defect

data class DefectDetailState(
    val defect: Defect? = null,
    val isLoading: Boolean = true
)