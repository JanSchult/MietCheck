package com.example.shared.presentation.settings

sealed interface SettingsIntent {
    data class BiometricToggled(val enabled: Boolean) : SettingsIntent
    data class TimeoutSelected(val seconds: Int) : SettingsIntent
    data class ScreenshotsToggled(val allowed: Boolean) : SettingsIntent
    data object LockNow : SettingsIntent
    data object EraseAllConfirmed : SettingsIntent
}