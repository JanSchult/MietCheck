package com.example.shared.data.repositorys

import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.repository.SelectedApartmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// TODO: später in den lokalen Einstellungen persistieren (letzte Auswahl nach App-Neustart)
class InMemorySelectedApartmentRepository : SelectedApartmentRepository {
    private val _selectedId = MutableStateFlow<ApartmentId?>(null)
    override val selectedId: StateFlow<ApartmentId?> = _selectedId.asStateFlow()

    override fun select(id: ApartmentId?) {
        _selectedId.value = id
    }
}