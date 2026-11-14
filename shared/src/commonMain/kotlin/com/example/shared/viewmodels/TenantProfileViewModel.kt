package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.TenantField
import com.example.shared.domain.model.TenantProfile
import com.example.shared.domain.usecase.GetTenantProfileUseCase
import com.example.shared.domain.usecase.SaveTenantProfileUseCase
import com.example.shared.viewmodels.effects.TenantProfileEffect
import com.example.shared.viewmodels.intent.TenantProfileIntent
import com.example.shared.viewmodels.state.TenantProfileState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TenantProfileViewModel(
    getProfile: GetTenantProfileUseCase,
    private val saveProfile: SaveTenantProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TenantProfileState())
    val state: StateFlow<TenantProfileState> = _state.asStateFlow()

    private val _effects = Channel<TenantProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            val p = getProfile()
            _state.update { it.copy(name = p.name, email = p.email, phone = p.phone, isLoading = false) }
        }
    }

    fun onIntent(intent: TenantProfileIntent) {
        when (intent) {
            is TenantProfileIntent.FieldChanged -> _state.update {
                val errors = it.errors - intent.field
                when (intent.field) {
                    TenantField.NAME -> it.copy(name = intent.value, errors = errors)
                    TenantField.EMAIL -> it.copy(email = intent.value, errors = errors)
                    TenantField.PHONE -> it.copy(phone = intent.value, errors = errors)
                }
            }
            TenantProfileIntent.Save -> save()
        }
    }

    private fun save() {
        val s = _state.value
        val errors = mutableMapOf<TenantField, String>()
        if (s.name.trim().length > 80) errors[TenantField.NAME] = "Der Name ist zu lang."
        if (s.email.isNotBlank() && !isPlausibleEmail(s.email.trim())) {
            errors[TenantField.EMAIL] = "Bitte eine gültige E-Mail-Adresse eingeben."
        }
        if (s.phone.isNotBlank() && !isPlausiblePhone(s.phone.trim())) {
            errors[TenantField.PHONE] = "Bitte nur Ziffern, Leerzeichen und + - ( ) / verwenden."
        }
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching { saveProfile(TenantProfile(s.name, s.email, s.phone)) }
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    _effects.send(TenantProfileEffect.Saved)
                }
                .onFailure { e ->
                    _state.update { it.copy(isSaving = false) }
                    _effects.send(TenantProfileEffect.ShowError(e.message ?: "Speichern fehlgeschlagen"))
                }
        }
    }

    private fun isPlausibleEmail(value: String): Boolean {
        val at = value.indexOf('@')
        return at > 0 && !value.contains(' ') && value.indexOf('.', at) > at + 1 && !value.endsWith(".")
    }

    private fun isPlausiblePhone(value: String): Boolean =
        value.length <= 30 && value.all { it.isDigit() || it in "+-() /" }
}