package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.ReminderId
import com.example.shared.domain.repository.ReminderListEffect
import com.example.shared.domain.repository.ReminderListIntent
import com.example.shared.domain.usecase.DeleteReminderUseCase
import com.example.shared.domain.usecase.MarkReminderCompletedUseCase
import com.example.shared.domain.usecase.ObserveRemindersUseCase
import com.example.shared.viewmodels.state.ReminderListState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVVM/MVI-ViewModel: nimmt Intents entgegen, hält den State als
 * StateFlow, sendet einmalige Effects (Navigation, Snackbar) über einen
 * Channel. Die Compose-UI kennt nur `state` und `effects` — kein
 * Repository- oder UseCase-Zugriff aus der UI-Schicht.
 */
class ReminderListViewModel(
    private val observeReminders: ObserveRemindersUseCase,
    private val markCompleted: MarkReminderCompletedUseCase,
    private val deleteReminder: DeleteReminderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderListState())
    val state: StateFlow<ReminderListState> = _state.asStateFlow()

    private val _effects = Channel<ReminderListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: ReminderListIntent) {
        when (intent) {
            is ReminderListIntent.LoadReminders -> loadReminders(intent.apartmentId)
            is ReminderListIntent.ToggleCompleted -> toggleCompleted(intent.id, intent.completed)
            is ReminderListIntent.DeleteReminder -> delete(intent.id)
            ReminderListIntent.AddReminderClicked -> viewModelScope.launch {
                _effects.send(ReminderListEffect.NavigateToAddReminder)
            }
        }
    }

    private fun loadReminders(apartmentIdRaw: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            observeReminders(ApartmentId(apartmentIdRaw))
                .collect { reminders ->
                    _state.update { it.copy(reminders = reminders, isLoading = false) }
                }
        }
    }

    private fun toggleCompleted(id: ReminderId, completed: Boolean) {
        viewModelScope.launch {
            runCatching { markCompleted(id, completed) }
                .onFailure { e ->
                    _effects.send(ReminderListEffect.ShowError(e.message ?: "Unbekannter Fehler"))
                }
        }
    }

    private fun delete(id: ReminderId) {
        viewModelScope.launch {
            runCatching { deleteReminder(id) }
                .onFailure { e ->
                    _effects.send(ReminderListEffect.ShowError(e.message ?: "Unbekannter Fehler"))
                }
        }
    }
}