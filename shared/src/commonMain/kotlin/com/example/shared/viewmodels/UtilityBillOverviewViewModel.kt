package com.example.shared.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.LetterTemplateType
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.usecase.BuildBillOverviewUseCase
import com.example.shared.domain.usecase.RunValidationUseCase
import com.example.shared.presentation.utilitybill.UtilityBillOverviewEffect
import com.example.shared.presentation.utilitybill.UtilityBillOverviewIntent
import com.example.shared.viewmodels.state.UtilityBillOverviewState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UtilityBillOverviewViewModel(
    private val buildOverview: BuildBillOverviewUseCase,
    private val runValidation: RunValidationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UtilityBillOverviewState())
    val state: StateFlow<UtilityBillOverviewState> = _state.asStateFlow()

    private val _effects = Channel<UtilityBillOverviewEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var currentBillId: UtilityBillId? = null

    fun onIntent(intent: UtilityBillOverviewIntent) {
        when (intent) {
            is UtilityBillOverviewIntent.LoadBill -> load(intent.billId)
            UtilityBillOverviewIntent.RunValidation -> validate()
            is UtilityBillOverviewIntent.StartLetterDraft -> startLetter(intent.templateType)
        }
    }

    /** Lädt die Übersicht und startet danach direkt die Prüfung (Speichern und prüfen). */
    private fun load(billIdRaw: String) {
        val id = UtilityBillId(billIdRaw)
        currentBillId = id
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val overview = buildOverview(id)
            if (overview == null) {
                _state.update { it.copy(isLoading = false) }
                _effects.send(UtilityBillOverviewEffect.ShowError("Abrechnung nicht gefunden."))
                return@launch
            }
            _state.update { it.copy(overview = overview, isLoading = false) }
            validate()
        }
    }

    private fun validate() {
        val id = currentBillId ?: return
        _state.update { it.copy(isValidating = true) }
        viewModelScope.launch {
            runCatching { runValidation(id) }
                .onSuccess { results ->
                    _state.update {
                        it.copy(validationResults = results, isValidating = false, hasValidated = true)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isValidating = false) }
                    _effects.send(UtilityBillOverviewEffect.ShowError(e.message ?: "Prüfung fehlgeschlagen"))
                }
        }
    }

    private fun startLetter(type: LetterTemplateType) {
        val id = currentBillId ?: return
        viewModelScope.launch {
            _effects.send(UtilityBillOverviewEffect.NavigateToLetterEditor(type, id.value))
        }
    }
}