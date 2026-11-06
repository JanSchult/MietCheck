package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.DocumentId
import com.example.shared.domain.usecase.DeleteDocumentUseCase
import com.example.shared.domain.usecase.GetDocumentUseCase
import com.example.shared.domain.usecase.ObserveApartmentsUseCase
import com.example.shared.domain.usecase.ShareDocumentUseCase
import com.example.shared.domain.usecase.UpdateDocumentUseCase
import com.example.shared.presentation.document.DocumentDetailEffect
import com.example.shared.presentation.document.DocumentDetailIntent
import com.example.shared.viewmodels.state.DocumentDetailState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentDetailViewModel(
    private val getDocument: GetDocumentUseCase,
    private val observeApartments: ObserveApartmentsUseCase,
    private val updateDocument: UpdateDocumentUseCase,
    private val deleteDocument: DeleteDocumentUseCase,
    private val shareDocument: ShareDocumentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentDetailState())
    val state: StateFlow<DocumentDetailState> = _state.asStateFlow()

    private val _effects = Channel<DocumentDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var documentId: DocumentId? = null

    fun onIntent(intent: DocumentDetailIntent) {
        when (intent) {
            is DocumentDetailIntent.Load -> load(intent.documentId)
            is DocumentDetailIntent.NameChanged -> _state.update { it.copy(name = intent.value, nameError = null) }
            is DocumentDetailIntent.TypeSelected -> _state.update { it.copy(type = intent.type) }
            is DocumentDetailIntent.ApartmentSelected -> _state.update { it.copy(apartmentId = intent.apartmentId) }
            DocumentDetailIntent.Save -> save()
            DocumentDetailIntent.Share -> share()
            DocumentDetailIntent.DeleteConfirmed -> delete()
        }
    }

    private fun message(text: String) {
        viewModelScope.launch { _effects.send(DocumentDetailEffect.ShowMessage(text)) }
    }

    private fun load(raw: String) {
        if (documentId?.value == raw) return          // Screen-Neuaufbau: Eingaben nicht überschreiben
        val id = DocumentId(raw)
        documentId = id
        viewModelScope.launch {
            val doc = getDocument(id)
            _state.update {
                if (doc == null) it.copy(isLoading = false)
                else it.copy(
                    document = doc,
                    name = doc.displayName,
                    type = doc.type,
                    apartmentId = doc.apartmentId.value,
                    isLoading = false
                )
            }
        }
        viewModelScope.launch {
            observeApartments().collect { list -> _state.update { it.copy(apartments = list) } }
        }
    }

    private fun save() {
        val id = documentId ?: return
        val s = _state.value
        val apartmentId = s.apartmentId ?: return
        if (s.name.isBlank()) {
            _state.update { it.copy(nameError = "Bitte einen Namen eingeben.") }
            return
        }
        viewModelScope.launch {
            runCatching { updateDocument(id, s.name, s.type, ApartmentId(apartmentId)) }
                .onSuccess { updated ->
                    if (updated != null) {
                        _state.update { it.copy(document = updated, name = updated.displayName) }
                        message("Änderungen gespeichert.")
                    }
                }
                .onFailure { e -> message(e.message ?: "Speichern fehlgeschlagen.") }
        }
    }

    private fun share() {
        val id = documentId ?: return
        viewModelScope.launch {
            runCatching { shareDocument(id) }
                .onFailure { e -> message(e.message ?: "Das Dokument konnte nicht geöffnet werden.") }
        }
    }

    private fun delete() {
        val id = documentId ?: return
        viewModelScope.launch {
            runCatching { deleteDocument(id) }
                .onSuccess { _effects.send(DocumentDetailEffect.Deleted) }
                .onFailure { e -> message(e.message ?: "Löschen fehlgeschlagen.") }
        }
    }
}