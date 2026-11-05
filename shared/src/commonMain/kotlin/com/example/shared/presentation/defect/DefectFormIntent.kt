package com.example.shared.presentation.defect

import com.example.shared.domain.model.DefectUrgency

sealed interface DefectFormIntent {
    data class RoomSelected(val room: String) : DefectFormIntent
    data class CustomRoomChanged(val value: String) : DefectFormIntent
    data class DescriptionChanged(val value: String) : DefectFormIntent
    data class UrgencySelected(val urgency: DefectUrgency) : DefectFormIntent
    data object Save : DefectFormIntent
}
