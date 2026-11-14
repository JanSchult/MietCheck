package com.example.shared.viewmodels.effects

sealed interface TenantProfileEffect {
    data object Saved : TenantProfileEffect
    data class ShowError(val message: String) : TenantProfileEffect
}