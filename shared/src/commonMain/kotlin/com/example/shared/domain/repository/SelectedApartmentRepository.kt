package com.example.shared.domain.repository

import com.example.shared.domain.model.ids.ApartmentId
import kotlinx.coroutines.flow.StateFlow

/** Geteilter Zustand: welche Wohnung ist gerade aktiv? Nutzen alle Tabs. */
interface SelectedApartmentRepository {
    val selectedId: StateFlow<ApartmentId?>
    fun select(id: ApartmentId?)
}