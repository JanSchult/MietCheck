package com.example.shared.viewmodels.state

import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.Reminder
import com.example.shared.domain.usecase.OpenCheck

data class DashboardState(
    val apartments: List<Apartment> = emptyList(),
    val selectedApartmentId: String? = null,
    val recentDocuments: List<DocumentRecord> = emptyList(),
    val openChecks: List<OpenCheck> = emptyList(),
    val upcomingReminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = true
)