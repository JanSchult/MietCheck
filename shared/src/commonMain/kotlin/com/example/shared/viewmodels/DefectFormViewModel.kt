package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.DefectStatus
import com.example.shared.domain.model.ids.DefectId
import com.example.shared.domain.repository.SelectedApartmentRepository
import com.example.shared.domain.usecase.SaveDefectUseCase
import com.example.shared.viewmodels.effects.DefectFormEffect
import com.example.shared.viewmodels.intent.DefectFormIntent
import com.example.shared.viewmodels.state.DefectFormState
import com.example.shared.viewmodels.state.OTHER_ROOM
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DefectFormViewModel(
    private val selectedApartment: SelectedApartmentRepository,
    private val saveDefect: SaveDefectUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DefectFormState())
    val state: StateFlow<DefectFormState> = _state.asStateFlow()

    private val _effects = Channel<DefectFormEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: DefectFormIntent) {
        when (intent) {
            is DefectFormIntent.RoomSelected -> _state.update { it.copy(room = intent.room, roomError = null) }
            is DefectFormIntent.CustomRoomChanged -> _state.update { it.copy(customRoom = intent.value, roomError = null) }
            is DefectFormIntent.DescriptionChanged ->
                _state.update { it.copy(description = intent.value, descriptionError = null) }
            is DefectFormIntent.UrgencySelected -> _state.update { it.copy(urgency = intent.urgency) }
            DefectFormIntent.Save -> save()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun save() {
        val s = _state.value
        val apartmentId = selectedApartment.selectedId.value
        if (apartmentId == null) {
            viewModelScope.launch { _effects.send(DefectFormEffect.ShowError("Keine Wohnung ausgewählt.")) }
            return
        }

        val room = if (s.room == OTHER_ROOM) s.customRoom.trim() else s.room
        val roomError = if (room.isEmpty()) "Bitte den Raum angeben." else null
        val descriptionError = if (s.description.isBlank()) "Bitte beschreibe den Mangel kurz." else null
        if (roomError != null || descriptionError != null) {
            _state.update { it.copy(roomError = roomError, descriptionError = descriptionError) }
            return
        }

        val defect = Defect(
            id = DefectId.new(),
            apartmentId = apartmentId,
            room = room,
            description = s.description.trim(),
            urgency = s.urgency,
            status = DefectStatus.NEW,
            mediaFilePaths = emptyList(),
            reportedAt = null,
            createdAt = Clock.System.now(),     // Datum wird automatisch gespeichert
            communications = emptyList()
        )

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching { saveDefect(defect) }
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    _effects.send(DefectFormEffect.Saved(defect.id.value))
                }
                .onFailure { e ->
                    _state.update { it.copy(isSaving = false) }
                    _effects.send(DefectFormEffect.ShowError(e.message ?: "Speichern fehlgeschlagen"))
                }
        }
    }
}