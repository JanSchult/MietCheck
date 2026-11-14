package com.example.shared.viewmodels.effects

sealed interface SettingsEffect { data class ShowMessage(val message: String) : SettingsEffect }