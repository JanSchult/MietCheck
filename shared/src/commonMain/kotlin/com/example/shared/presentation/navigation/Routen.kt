package com.example.shared.presentation.navigation

import kotlinx.serialization.Serializable

// Bottom-Nav-Ziele (Top-Level)
@Serializable data object DashboardRoute
@Serializable data object DocumentsRoute
@Serializable data object ChecksRoute
@Serializable data object DefectsRoute

// Detail- und Flow-Ziele
@Serializable data object AddApartmentRoute
@Serializable data class BillRoute(val billId: String)
@Serializable data class LetterEditorRoute(val billId: String, val templateType: String)
@Serializable data class RemindersRoute(val apartmentId: String)