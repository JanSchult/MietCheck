package com.example.shared.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.DocumentType
import com.example.shared.domain.model.ImportedFile
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.repository.SelectedApartmentRepository
import com.example.shared.domain.usecase.ImportDocumentUseCase
import com.example.shared.domain.usecase.ImportResult
import com.example.shared.domain.usecase.ObserveDocumentsUseCase
import com.example.shared.viewmodels.state.DocumentsEffect
import com.example.shared.viewmodels.state.DocumentsIntent
import com.example.shared.viewmodels.state.DocumentsState
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
class DocumentsViewModel(
    private val selectedApartment: SelectedApartmentRepository,
    observeDocuments: ObserveDocumentsUseCase,
    private val importDocument: ImportDocumentUseCase
) : ViewModel() {

    private val filter = MutableStateFlow<DocumentType?>(null)
    private val importing = MutableStateFlow(false)

    val state: StateFlow<DocumentsState> = combine(
        selectedApartment.selectedId.flatMapLatest { id ->
            if (id == null) {
                flowOf(Pair<ApartmentId?, List<DocumentRecord>>(null, emptyList()))
            } else {
                observeDocuments(id).map { Pair<ApartmentId?, List<DocumentRecord>>(id, it) }
            }
        },
        filter,
        importing
    ) { pair, type, busy ->
        DocumentsState(
            apartmentId = pair.first?.value,
            documents = pair.second,
            filter = type,
            isLoading = false,
            isImporting = busy
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DocumentsState())

    private val _effects = Channel<DocumentsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onIntent(intent: DocumentsIntent) {
        when (intent) {
            is DocumentsIntent.FilterChanged -> filter.value = intent.type
            is DocumentsIntent.DocumentClicked ->
                viewModelScope.launch { _effects.send(DocumentsEffect.NavigateToDetail(intent.id)) }
            is DocumentsIntent.Import -> import(intent.file, intent.type)
        }
    }

    private fun message(text: String) {
        viewModelScope.launch { _effects.send(DocumentsEffect.ShowMessage(text)) }
    }

    private fun import(file: ImportedFile, type: DocumentType) {
        val apartmentId = selectedApartment.selectedId.value
        if (apartmentId == null) {
            message("Keine Wohnung ausgewählt.")
            return
        }
        importing.value = true
        viewModelScope.launch {
            runCatching { importDocument(apartmentId, type, file) }
                .onSuccess { result ->
                    message(
                        when (result) {
                            is ImportResult.Saved -> "Dokument gespeichert."
                            is ImportResult.Duplicate ->
                                "Dieses Dokument ist bereits als „${result.existingName}“ gespeichert."
                        }
                    )
                }
                .onFailure { e -> message(e.message ?: "Der Import ist fehlgeschlagen.") }
            importing.value = false
        }
    }
}