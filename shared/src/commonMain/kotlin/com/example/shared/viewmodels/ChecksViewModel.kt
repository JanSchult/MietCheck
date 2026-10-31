package com.example.shared.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.BillSummary
import com.example.shared.domain.model.ValidationSeverity
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.repository.SelectedApartmentRepository
import com.example.shared.domain.usecase.ObserveBillsUseCase
import com.example.shared.domain.usecase.RunValidationUseCase
import com.example.shared.presentation.checks.ChecksEffect
import com.example.shared.presentation.checks.ChecksIntent
import com.example.shared.viewmodels.state.ChecksState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChecksViewModel(
    selectedApartment: SelectedApartmentRepository,
    private val observeBills: ObserveBillsUseCase,
    private val runValidation: RunValidationUseCase
) : ViewModel() {

    val state: StateFlow<ChecksState> = selectedApartment.selectedId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(ChecksState(isLoading = false))
            } else {
                observeBills(id).map { bills ->
                    ChecksState(
                        apartmentId = id.value,
                        bills = bills.map { BillSummary(it, countFindings(it.id)) },
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChecksState())

    private val _effects = Channel<ChecksEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: ChecksIntent) {
        when (intent) {
            ChecksIntent.AddBillClicked -> state.value.apartmentId?.let { id ->
                viewModelScope.launch { _effects.send(ChecksEffect.NavigateToForm(id)) }
            }
            is ChecksIntent.BillClicked ->
                viewModelScope.launch { _effects.send(ChecksEffect.NavigateToBill(intent.billId)) }
        }
    }

    private suspend fun countFindings(billId: UtilityBillId): Int? =
        runCatching { runValidation(billId).count { it.severity != ValidationSeverity.OK } }.getOrNull()
}