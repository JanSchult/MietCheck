package com.example.shared.viewmodels.intent

import com.example.shared.domain.model.TenantField

sealed interface TenantProfileIntent {
    data class FieldChanged(val field: TenantField, val value: String) : TenantProfileIntent
    data object Save : TenantProfileIntent
}