package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.repository.SelectedApartmentRepository
import com.example.shared.domain.usecase.GetOpenChecksUseCase
import com.example.shared.domain.usecase.ObserveApartmentsUseCase
import com.example.shared.domain.usecase.ObserveRecentDocumentsUseCase
import com.example.shared.domain.usecase.ObserveRemindersUseCase
import com.example.shared.presentation.dashboard.DashboardEffect
import com.example.shared.viewmodels.state.DashboardState
import com.example.shared.presentation.dashboard.DashboardIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class DashboardViewModel(
    private val observeApartments: ObserveApartmentsUseCase,
    private val observeRecentDocuments: ObserveRecentDocumentsUseCase,
    private val observeReminders: ObserveRemindersUseCase,
    private val getOpenChecks: GetOpenChecksUseCase,
    private val selectedApartment: SelectedApartmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val selectedId = selectedApartment.selectedId

    init {
        // Wohnungen beobachten; erste Wohnung automatisch wählen
        viewModelScope.launch {
            observeApartments().collect { apartments ->
                if (selectedId.value == null || apartments.none { it.id == selectedId.value }) {
                    selectedApartment.select(apartments.firstOrNull()?.id)
                }
                _state.update {
                    it.copy(apartments = apartments, isLoading = false)
                }
            }
        }

        // Dokumente + Fristen der gewählten Wohnung
        viewModelScope.launch {
            selectedId.flatMapLatest { id ->
                if (id == null) {
                    flowOf(Triple(null, emptyList(), emptyList()))
                } else {
                    combine(observeRecentDocuments(id), observeReminders(id)) { docs, reminders ->
                        Triple(id, docs, reminders)
                    }
                }
            }.collect { (id, docs, reminders) ->
                _state.update {
                    it.copy(
                        selectedApartmentId = id?.value,
                        recentDocuments = docs,
                        upcomingReminders = reminders
                            .filter { r -> !r.isCompleted }
                            .sortedBy { r -> r.dueAt }
                            .take(5)
                    )
                }
                if (id != null) refreshOpenChecks(id)
            }
        }
    }

    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.SelectApartment ->
                selectedApartment.select(ApartmentId(intent.apartmentId))

            is DashboardIntent.QuickActionClicked -> viewModelScope.launch {
                _effects.send(DashboardEffect.StartQuickAction(intent.action, _state.value.selectedApartmentId))
            }
            DashboardIntent.AddApartmentClicked -> viewModelScope.launch {
                _effects.send(DashboardEffect.NavigateToAddApartment)
            }
            is DashboardIntent.OpenCheckClicked -> viewModelScope.launch {
                _effects.send(DashboardEffect.NavigateToBill(intent.billId))
            }
        }
    }

    /** Prüfungen sind keine Flows, deshalb bei jedem Wechsel/Update neu berechnen. */
    private suspend fun refreshOpenChecks(apartmentId: ApartmentId) {
        val checks = runCatching { getOpenChecks(apartmentId) }.getOrDefault(emptyList())
        _state.update { it.copy(openChecks = checks) }
    }
}