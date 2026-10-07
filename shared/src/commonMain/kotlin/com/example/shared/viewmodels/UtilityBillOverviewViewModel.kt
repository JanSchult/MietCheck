package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.usecase.GetUtilityBillUseCase
import com.example.shared.domain.usecase.RunValidationUseCase
import com.example.shared.validation.UtilityBillOverviewEffect
import com.example.shared.validation.UtilityBillOverviewIntent
import com.example.shared.viewmodels.state.UtilityBillOverviewState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UtilityBillOverviewViewModel(
    private val getBill: GetUtilityBillUseCase,
    private val runValidation: RunValidationUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(UtilityBillOverviewState())
    val state: StateFlow<UtilityBillOverviewState> = _state.asStateFlow()

    private val _effects = Channel<UtilityBillOverviewEffect>()
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: UtilityBillOverviewIntent) {
        when (intent) {
            is UtilityBillOverviewIntent.LoadBill -> loadBill(intent.billId)
            is UtilityBillOverviewIntent.RunValidation -> validate(intent)
            is UtilityBillOverviewIntent.StartLetterDraft -> viewModelScope.launch {
                _effects.send(UtilityBillOverviewEffect.NavigateToLetterEditor(intent.templateType.name))
            }
        }
    }

    private fun loadBill(id: UtilityBillId) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        val bill = getBill(id)
        _state.update { it.copy(bill = bill, isLoading = false) }
    }

    private fun validate(intent: UtilityBillOverviewIntent.RunValidation) = viewModelScope.launch {
        val current = _state.value.bill ?: return@launch
        val results = runValidation(current.id)
        _state.update { it.copy(validationResults = results) }
    }
}