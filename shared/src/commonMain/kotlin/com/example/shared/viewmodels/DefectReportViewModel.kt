package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.ids.DefectId
import com.example.shared.domain.platform.FileSharer
import com.example.shared.domain.platform.PdfTextWriter
import com.example.shared.domain.platform.TextSharer
import com.example.shared.domain.usecase.BuildDefectReportUseCase
import com.example.shared.viewmodels.effects.DefectReportEffect
import com.example.shared.viewmodels.intent.DefectReportIntent
import com.example.shared.viewmodels.state.DefectReportState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DefectReportViewModel(
    private val buildDraft: BuildDefectReportUseCase,
    private val textSharer: TextSharer,
    private val fileSharer: FileSharer,
    private val pdfWriter: PdfTextWriter
) : ViewModel() {

    private val _state = MutableStateFlow(DefectReportState())
    val state: StateFlow<DefectReportState> = _state.asStateFlow()

    private val _effects = Channel<DefectReportEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var loadedId: String? = null

    fun onIntent(intent: DefectReportIntent) {
        when (intent) {
            is DefectReportIntent.Load -> load(intent.defectId)
            is DefectReportIntent.SubjectChanged ->
                _state.update { it.copy(subject = intent.value, isConfirmed = false) }
            is DefectReportIntent.BodyChanged -> _state.update {
                it.copy(body = intent.value, isConfirmed = false, hasOpenPlaceholders = hasPlaceholders(intent.value))
            }
            is DefectReportIntent.ConfirmedChanged -> _state.update { it.copy(isConfirmed = intent.value) }
            DefectReportIntent.ShareText -> shareText()
            DefectReportIntent.SharePdf -> sharePdf()
        }
    }

    private fun message(text: String) {
        viewModelScope.launch { _effects.send(DefectReportEffect.ShowMessage(text)) }
    }

    private fun load(raw: String) {
        if (loadedId == raw) return                    // Neuaufbau des Screens: Bearbeitung nicht überschreiben
        loadedId = raw
        viewModelScope.launch {
            val draft = buildDraft(DefectId(raw))
            _state.update {
                if (draft == null) it.copy(isLoading = false, notFound = true)
                else it.copy(
                    subject = draft.subject,
                    body = draft.body,
                    isLoading = false,
                    hasOpenPlaceholders = hasPlaceholders(draft.body)
                )
            }
        }
    }

    private fun shareText() {
        val s = _state.value
        if (!s.isConfirmed) return                     // ohne Bestätigung geht nichts raus
        textSharer.shareText(s.subject, s.body)
    }

    private fun sharePdf() {
        val s = _state.value
        if (!s.isConfirmed || s.isWorking) return
        _state.update { it.copy(isWorking = true) }
        viewModelScope.launch {
            runCatching {
                val bytes = pdfWriter.write(s.body)
                fileSharer.shareFile(bytes, "Maengelanzeige.pdf", "application/pdf")
            }.onFailure { e -> message(e.message ?: "Das PDF konnte nicht erstellt werden.") }
            _state.update { it.copy(isWorking = false) }
        }
    }

    private fun hasPlaceholders(text: String): Boolean =
        text.contains("{{") || text.contains("[Dein") || text.contains("[Vermieter") || text.contains("[Adresse")
}