package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.lock.AppLockManager
import com.example.shared.domain.lock.PIN_LENGTH
import com.example.shared.domain.lock.PinCheck
import com.example.shared.domain.lock.PinMode
import com.example.shared.domain.lock.PinStep
import com.example.shared.domain.lock.isWeakPin
import com.example.shared.domain.platform.BiometricAuthenticator
import com.example.shared.viewmodels.effects.PinSetupEffect
import com.example.shared.viewmodels.intent.PinSetupIntent
import com.example.shared.viewmodels.state.PinSetupState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PinSetupViewModel(
    private val manager: AppLockManager,
    private val authenticator: BiometricAuthenticator
) : ViewModel() {

    private val _state = MutableStateFlow(PinSetupState())
    val state: StateFlow<PinSetupState> = _state.asStateFlow()

    private val _effects = Channel<PinSetupEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var buffer = ""
    private var firstEntry = ""
    private var offerBiometric = false
    private var initKey: String? = null
    private var tickerJob: Job? = null

    fun onIntent(intent: PinSetupIntent) {
        when (intent) {
            is PinSetupIntent.Init -> init(intent.mode, intent.offerBiometric)
            is PinSetupIntent.Digit -> digit(intent.value)
            PinSetupIntent.Backspace -> if (buffer.isNotEmpty() && !_state.value.isBusy) {
                buffer = buffer.dropLast(1)
                _state.update { it.copy(digits = buffer.length, error = null) }
            }
            is PinSetupIntent.BiometricChoice -> biometricChoice(intent.enable)
        }
    }

    private fun init(mode: PinMode, offer: Boolean) {
        val key = "$mode/$offer"
        if (initKey == key) return                      // Drehung: Fortschritt behalten
        initKey = key
        buffer = ""; firstEntry = ""; offerBiometric = offer
        val first = if (mode == PinMode.SETUP) PinStep.ENTER_NEW else PinStep.VERIFY_CURRENT
        _state.value = PinSetupState(mode = mode, step = first)
        startTicker()
    }

    private fun digit(value: Int) {
        val s = _state.value
        if (s.isBusy || s.lockoutSeconds > 0 || s.step == PinStep.OFFER_BIOMETRIC || buffer.length >= PIN_LENGTH) return
        buffer += value.toString()
        _state.update { it.copy(digits = buffer.length, error = null) }
        if (buffer.length == PIN_LENGTH) complete()
    }

    private fun complete() {
        val entered = buffer
        buffer = ""
        when (_state.value.step) {
            PinStep.VERIFY_CURRENT -> verifyCurrent(entered)
            PinStep.ENTER_NEW -> {
                if (isWeakPin(entered)) {
                    _state.update { it.copy(digits = 0, error = "Diese PIN ist leicht zu erraten. Bitte wähle eine andere.") }
                } else {
                    firstEntry = entered
                    _state.update { it.copy(step = PinStep.CONFIRM_NEW, digits = 0) }
                }
            }
            PinStep.CONFIRM_NEW -> confirmNew(entered)
            PinStep.OFFER_BIOMETRIC -> Unit
        }
    }

    private fun verifyCurrent(entered: String) {
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val result = manager.checkPin(entered)
            _state.update { it.copy(isBusy = false, digits = 0) }
            when (result) {
                PinCheck.Correct ->
                    if (_state.value.mode == PinMode.REMOVE) {
                        manager.removePin()
                        _effects.send(PinSetupEffect.Done)
                    } else {
                        _state.update { it.copy(step = PinStep.ENTER_NEW) }
                    }
                is PinCheck.Wrong -> _state.update {
                    it.copy(error = "Falsche PIN. Noch ${result.attemptsLeftBeforeDelay} Versuch(e) bis zur Sperre.")
                }
                is PinCheck.LockedOut -> startTicker()
            }
        }
    }

    private fun confirmNew(entered: String) {
        if (entered != firstEntry) {
            firstEntry = ""
            _state.update { it.copy(step = PinStep.ENTER_NEW, digits = 0, error = "Die PINs stimmen nicht überein. Bitte neu festlegen.") }
            return
        }
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            runCatching { manager.setPin(entered) }
                .onSuccess {
                    firstEntry = ""
                    if (offerBiometric && authenticator.isAvailable()) {
                        _state.update { it.copy(isBusy = false, step = PinStep.OFFER_BIOMETRIC, digits = 0) }
                    } else {
                        _state.update { it.copy(isBusy = false) }
                        _effects.send(PinSetupEffect.Done)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isBusy = false, step = PinStep.ENTER_NEW, digits = 0, error = e.message) }
                }
        }
    }

    private fun biometricChoice(enable: Boolean) {
        viewModelScope.launch {
            if (enable) {
                if (!authenticator.authenticate()) {
                    _state.update { it.copy(error = "Die Bestätigung hat nicht geklappt. Du kannst es später in den Einstellungen aktivieren.") }
                    return@launch
                }
                manager.setBiometricEnabled(true)
            }
            _effects.send(PinSetupEffect.Done)
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            manager.lockoutTicker().collect { seconds -> _state.update { it.copy(lockoutSeconds = seconds) } }
        }
    }
}