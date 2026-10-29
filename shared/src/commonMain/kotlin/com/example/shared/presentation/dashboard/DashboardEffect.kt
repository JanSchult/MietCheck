package com.example.shared.presentation.dashboard

import com.example.shared.domain.model.QuickAction

sealed interface DashboardEffect {
    data class StartQuickAction(val action: QuickAction, val apartmentId: String?) : DashboardEffect
    data object NavigateToAddApartment : DashboardEffect
    data class NavigateToBill(val billId: String) : DashboardEffect
    data class NavigateToReminders(val apartmentId: String) : DashboardEffect
}