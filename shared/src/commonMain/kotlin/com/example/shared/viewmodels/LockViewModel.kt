package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.lock.AppLockManager
import com.example.shared.domain.lock.PIN_LENGTH
import com.example.shared.domain.lock.PinCheck
import com.example.shared.domain.platform.AppDataEraser
import com.example.shared.domain.platform.BiometricAuthenticator
import com.example.shared.viewmodels.intent.LockIntent
import com.example.shared.viewmodels.state.LockUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LockViewModel(
    private val manager: AppLockManager,
    private val authenticator: BiometricAuthenticator,
    private val eraser: AppDataEraser
) : ViewModel() {

    private val _state = MutableStateFlow(LockUiState())
    val state: StateFlow<LockUiState> = _state.asStateFlow()

    private var pin = ""                // die PIN steht nie im State
    private var tickerJob: Job? = null

    fun onIntent(intent: LockIntent) {
        when (intent) {
            LockIntent.Entered -> entered()
            is LockIntent.Digit -> digit(intent.value)
            LockIntent.Backspace -> if (pin.isNotEmpty() && !_state.value.isChecking) {
                pin = pin.dropLast(1)
                _state.update { it.copy(enteredDigits = pin.length, error = null) }
            }
            LockIntent.BiometricRequested -> requestBiometric()
            LockIntent.EraseAllConfirmed -> eraser.eraseAll()
        }
    }

    /** Wird bei jedem Anzeigen des Sperrbildschirms aufgerufen (das ViewModel überlebt mehrere Sperren). */
    private fun entered() {
        pin = ""
        val available = manager.config.value.biometricEnabled && authenticator.isAvailable()
        _state.value = LockUiState(biometricAvailable = available)
        startTicker()
        if (available && manager.lockoutRemainingSeconds() == 0L) requestBiometric()
    }

    private fun digit(value: Int) {
        val s = _state.value
        if (s.isChecking || s.lockoutSeconds > 0 || pin.length >= PIN_LENGTH) return
        pin += value.toString()
        _state.update { it.copy(enteredDigits = pin.length, error = null) }
        if (pin.length == PIN_LENGTH) verify()
    }

    private fun verify() {
        val entered = pin
        _state.update { it.copy(isChecking = true) }
        viewModelScope.launch {
            val result = manager.unlockWithPin(entered)
            pin = ""
            _state.update { it.copy(isChecking = false, enteredDigits = 0) }
            when (result) {
                PinCheck.Correct -> Unit          // AppRoot wechselt den Screen
                is PinCheck.Wrong -> _state.update { it.copy(error = wrongMessage(result.attemptsLeftBeforeDelay)) }
                is PinCheck.LockedOut -> startTicker()
            }
        }
    }

    private fun requestBiometric() {
        viewModelScope.launch { if (authenticator.authenticate()) manager.unlock() }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            manager.lockoutTicker().collect { seconds ->
                _state.update { it.copy(lockoutSeconds = seconds, error = if (seconds > 0) null else it.error) }
            }
        }
    }

    private fun wrongMessage(left: Int) =
        if (left == 1) "Falsche PIN. Noch 1 Versuch, danach wird die Eingabe kurz gesperrt."
        else "Falsche PIN. Noch $left Versuche, danach wird die Eingabe kurz gesperrt."
}