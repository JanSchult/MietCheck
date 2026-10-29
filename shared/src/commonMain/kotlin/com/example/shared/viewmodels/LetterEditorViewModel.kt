package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.LetterTemplateType
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.usecase.BuildLetterContextUseCase
import com.example.shared.domain.usecase.BuildLetterDraftUseCase
import com.example.shared.presentation.letter.LetterEditorEffect
import com.example.shared.presentation.letter.LetterEditorIntent
import com.example.shared.viewmodels.state.LetterEditorState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LetterEditorViewModel(
    private val buildContext: BuildLetterContextUseCase,
    private val buildDraft: BuildLetterDraftUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LetterEditorState())
    val state: StateFlow<LetterEditorState> = _state.asStateFlow()

    private val _effects = Channel<LetterEditorEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: LetterEditorIntent) {
        when (intent) {
            is LetterEditorIntent.Load -> load(intent.billId, intent.templateTypeName)
            is LetterEditorIntent.SubjectChanged -> _state.update { it.copy(subject = intent.value) }
            is LetterEditorIntent.BodyChanged -> _state.update {
                it.copy(body = intent.value, hasOpenPlaceholders = hasPlaceholders(intent.value))
            }
            LetterEditorIntent.ConfirmAndExport -> export()
        }
    }

    private fun load(billIdRaw: String, templateTypeName: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val context = buildContext(UtilityBillId(billIdRaw))
            if (context == null) {
                _state.update { it.copy(isLoading = false) }
                _effects.send(LetterEditorEffect.ShowError("Abrechnung nicht gefunden."))
                return@launch
            }
            val type = runCatching { LetterTemplateType.valueOf(templateTypeName) }
                .getOrDefault(LetterTemplateType.GENERAL_INQUIRY)
            val draft = buildDraft(type, context)
            _state.update {
                it.copy(
                    subject = draft.subject,
                    body = draft.body,
                    isLoading = false,
                    hasOpenPlaceholders = hasPlaceholders(draft.body)
                )
            }
        }
    }


    private fun export() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isConfirmed = true) }
            _effects.send(LetterEditorEffect.Export(s.subject, s.body))
        }
    }

    private fun hasPlaceholders(text: String): Boolean =
        text.contains("{{") || text.contains("[Hier") || text.contains("[Dein") ||
                text.contains("[Vermieter") || text.contains("[Adresse")
}