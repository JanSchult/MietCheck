package com.example.shared.presentation.dashboard

import com.example.shared.domain.model.QuickAction

sealed interface DashboardIntent {
    data class SelectApartment(val apartmentId: String) : DashboardIntent
    data class QuickActionClicked(val action: QuickAction) : DashboardIntent
    data object AddApartmentClicked : DashboardIntent
    data class OpenCheckClicked(val billId: String) : DashboardIntent
}