package com.example.shared.viewmodels.state

import com.example.shared.domain.model.TenantField

data class TenantProfileState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val errors: Map<TenantField, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)
