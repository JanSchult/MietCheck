package com.example.shared.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.RecurrenceRule
import com.example.shared.domain.model.Reminder
import com.example.shared.domain.model.ReminderType
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.ReminderId
import com.example.shared.domain.usecase.SaveReminderUseCase
import com.example.shared.presentation.reminder.ReminderFormEffect
import com.example.shared.presentation.reminder.ReminderFormIntent
import com.example.shared.presentation.reminder.displayName
import com.example.shared.viewmodels.state.ReminderFormState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.todayIn
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

class ReminderFormViewModel(
    private val saveReminder: SaveReminderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderFormState())
    val state: StateFlow<ReminderFormState> = _state.asStateFlow()

    private val _effects = Channel<ReminderFormEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var apartmentId: ApartmentId? = null

    fun onIntent(intent: ReminderFormIntent) {
        when (intent) {
            is ReminderFormIntent.Init -> apartmentId = ApartmentId(intent.apartmentId)
            is ReminderFormIntent.TitleChanged -> _state.update { it.copy(title = intent.value) }
            is ReminderFormIntent.TypeChanged -> _state.update {
                it.copy(
                    type = intent.type,
                    // Zählerstände wiederholen sich in der Regel: sinnvoller Vorschlag
                    recurrence = if (intent.type == ReminderType.METER_READING_RECURRING &&
                        it.recurrence == RecurrenceRule.NONE
                    ) RecurrenceRule.YEARLY else it.recurrence
                )
            }
            is ReminderFormIntent.DueDateChanged ->
                _state.update { it.copy(dueDate = intent.value, dateError = null) }
            is ReminderFormIntent.RecurrenceChanged -> _state.update { it.copy(recurrence = intent.rule) }
            ReminderFormIntent.Save -> save()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun save() {
        val id = apartmentId ?: return
        val s = _state.value
        val zone = TimeZone.currentSystemDefault()

        val date = runCatching { LocalDate.parse(s.dueDate.trim()) }.getOrNull()
        val dateError = when {
            date == null -> "Bitte das Datum als JJJJ-MM-TT eingeben."
            date < kotlin.time.Clock.System.todayIn(zone) -> "Das Datum liegt in der Vergangenheit."
            else -> null
        }
        if (dateError != null) {
            _state.update { it.copy(dateError = dateError) }
            return
        }

        val reminder = Reminder(
            id = ReminderId.new(),
            apartmentId = id,
            type = s.type,
            title = s.title.trim().ifBlank { s.type.displayName() },
            dueAt = date!!.atTime(9, 0).toInstant(zone),
            recurrenceRule = s.recurrence,
            isCompleted = false,
            linkedEntityId = null
        )

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching { saveReminder(reminder) }
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    _effects.send(ReminderFormEffect.Saved)
                }
                .onFailure { e ->
                    _state.update { it.copy(isSaving = false) }
                    _effects.send(ReminderFormEffect.ShowError(e.message ?: "Speichern fehlgeschlagen"))
                }
        }
    }
}