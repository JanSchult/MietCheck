package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.lock.AppLockManager
import com.example.shared.domain.lock.LockConfig
import com.example.shared.domain.platform.AppDataEraser
import com.example.shared.domain.platform.BiometricAuthenticator
import com.example.shared.domain.usecase.ExportAllDataUseCase
import com.example.shared.presentation.settings.SettingsEffect
import com.example.shared.presentation.settings.SettingsIntent
import com.example.shared.viewmodels.state.SettingsState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val manager: AppLockManager,
    private val authenticator: BiometricAuthenticator,
    private val eraser: AppDataEraser,
    private val exportAllData : ExportAllDataUseCase
) : ViewModel() {

    private val exporting =MutableStateFlow(false)
    private fun LockConfig.toState(busy: Boolean) = SettingsState(
        hasPin = hasPin,
        biometricAvailable = authenticator.isAvailable(),
        biometricEnabled = biometricEnabled,
        timeoutSeconds = timeoutSeconds,
        screenshotsAllowed = screenshotsAllowed,
        isExporting = busy
    )

    val state: StateFlow<SettingsState> = combine(manager.config,exporting){config,busy ->config.toState(busy)}
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), manager.config.value.toState(false))

    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.BiometricToggled -> viewModelScope.launch {
                if (intent.enabled && !authenticator.authenticate()) {
                    _effects.send(SettingsEffect.ShowMessage("Die Bestätigung hat nicht geklappt."))
                } else {
                    manager.setBiometricEnabled(intent.enabled)
                }
            }
            is SettingsIntent.TimeoutSelected -> viewModelScope.launch { manager.setTimeoutSeconds(intent.seconds) }
            is SettingsIntent.ScreenshotsToggled -> viewModelScope.launch { manager.setScreenshotsAllowed(intent.allowed) }
            SettingsIntent.LockNow -> manager.lock()
            SettingsIntent.EraseAllConfirmed -> eraser.eraseAll()
            SettingsIntent.ExportConfirmed -> viewModelScope.launch {
                exporting.value = true
                runCatching { exportAllData() }
                    .onSuccess { summary ->
                        if (summary.missingDocuments > 0) {
                            _effects.send(SettingsEffect.ShowMessage(
                                "${summary.missingDocuments} Dokument(e) konnten nicht gelesen werden und fehlen im Export."
                            ))
                        }
                    }
                    .onFailure { e -> _effects.send(SettingsEffect.ShowMessage(e.message ?: "Der Export ist fehlgeschlagen.")) }
                exporting.value = false
            }
        }
    }
}