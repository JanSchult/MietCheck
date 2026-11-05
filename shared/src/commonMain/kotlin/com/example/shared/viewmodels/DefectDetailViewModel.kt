package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.ids.DefectId
import com.example.shared.domain.usecase.AddCommunicationUseCase
import com.example.shared.domain.usecase.DeleteDefectUseCase
import com.example.shared.domain.usecase.ObserveDefectUseCase
import com.example.shared.domain.usecase.UpdateDefectStatusUseCase
import com.example.shared.presentation.defect.DefectDetailEffect
import com.example.shared.presentation.defect.DefectDetailIntent
import com.example.shared.viewmodels.state.DefectDetailState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DefectDetailViewModel(
    private val observeDefect: ObserveDefectUseCase,
    private val updateStatus: UpdateDefectStatusUseCase,
    private val addCommunication: AddCommunicationUseCase,
    private val deleteDefect: DeleteDefectUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DefectDetailState())
    val state: StateFlow<DefectDetailState> = _state.asStateFlow()

    private val _effects = Channel<DefectDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var defectId: DefectId? = null
    private var loadJob: Job? = null

    fun onIntent(intent: DefectDetailIntent) {
        when (intent) {
            is DefectDetailIntent.Load -> load(intent.defectId)
            is DefectDetailIntent.StatusSelected -> run { updateStatus(it, intent.status) }
            is DefectDetailIntent.AddCommunication -> run { addCommunication(it, intent.direction, intent.summary) }
            DefectDetailIntent.DeleteConfirmed -> viewModelScope.launch {
                val id = defectId ?: return@launch
                runCatching { deleteDefect(id) }
                    .onSuccess { _effects.send(DefectDetailEffect.Deleted) }
                    .onFailure { e -> _effects.send(DefectDetailEffect.ShowError(e.message ?: "Löschen fehlgeschlagen")) }
            }
        }
    }

    private fun load(raw: String) {
        if (defectId?.value == raw && loadJob?.isActive == true) return
        val id = DefectId(raw)
        defectId = id
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            observeDefect(id).collect { defect ->
                _state.update { it.copy(defect = defect, isLoading = false) }
            }
        }
    }

    private fun run(block: suspend (DefectId) -> Unit) {
        val id = defectId ?: return
        viewModelScope.launch {
            runCatching { block(id) }
                .onFailure { e -> _effects.send(DefectDetailEffect.ShowError(e.message ?: "Aktion fehlgeschlagen")) }
        }
    }
}