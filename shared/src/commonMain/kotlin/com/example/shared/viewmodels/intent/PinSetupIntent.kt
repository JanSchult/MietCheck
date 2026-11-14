package com.example.shared.viewmodels.intent

import com.example.shared.domain.lock.PinMode

sealed interface PinSetupIntent {
    data class Init(val mode: PinMode, val offerBiometric: Boolean) : PinSetupIntent
    data class Digit(val value: Int) : PinSetupIntent
    data object Backspace : PinSetupIntent
    data class BiometricChoice(val enable: Boolean) : PinSetupIntent
}