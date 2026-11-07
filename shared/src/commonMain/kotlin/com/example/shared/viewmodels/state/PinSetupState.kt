package com.example.shared.viewmodels.state

import com.example.shared.domain.lock.PinMode
import com.example.shared.domain.lock.PinStep

data class PinSetupState(
    val mode: PinMode = PinMode.SETUP,
    val step: PinStep = PinStep.ENTER_NEW,
    val digits: Int = 0,
    val error: String? = null,
    val lockoutSeconds: Long = 0,
    val isBusy: Boolean = false
)