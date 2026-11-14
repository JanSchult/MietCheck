package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.DefectStatus
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.repository.SelectedApartmentRepository
import com.example.shared.domain.usecase.ObserveDefectsUseCase
import com.example.shared.viewmodels.effects.DefectsEffect
import com.example.shared.viewmodels.intent.DefectsIntent
import com.example.shared.viewmodels.state.DefectsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DefectsViewModel(
    selectedApartment: SelectedApartmentRepository,
    private val observeDefects: ObserveDefectsUseCase
) : ViewModel() {

    private val filter = MutableStateFlow<DefectStatus?>(null)

    val state: StateFlow<DefectsState> = combine(
        selectedApartment.selectedId.flatMapLatest { id ->
            if (id == null) {
                flowOf(Pair<ApartmentId?, List<Defect>>(null, emptyList()))
            } else {
                observeDefects(id).map { list -> Pair<ApartmentId?, List<Defect>>(id, list) }
            }
        },
        filter
    ) { pair, status ->
        DefectsState(
            apartmentId = pair.first?.value,
            defects = pair.second,
            filter = status,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DefectsState())

    private val _effects = Channel<DefectsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: DefectsIntent) {
        when (intent) {
            DefectsIntent.AddClicked -> viewModelScope.launch { _effects.send(DefectsEffect.NavigateToForm) }
            is DefectsIntent.DefectClicked ->
                viewModelScope.launch { _effects.send(DefectsEffect.NavigateToDetail(intent.id)) }
            is DefectsIntent.FilterChanged -> filter.value = intent.status
        }
    }
}