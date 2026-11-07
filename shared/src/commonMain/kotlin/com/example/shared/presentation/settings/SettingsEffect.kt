package com.example.shared.presentation.settings

sealed interface SettingsEffect { data class ShowMessage(val message: String) : SettingsEffect }
