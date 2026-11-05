package com.example.shared.presentation.defect

import com.example.shared.domain.model.CommunicationDirection
import com.example.shared.domain.model.DefectStatus

sealed interface DefectDetailIntent {
    data class Load(val defectId: String) : DefectDetailIntent
    data class StatusSelected(val status: DefectStatus) : DefectDetailIntent
    data class AddCommunication(val direction: CommunicationDirection, val summary: String) : DefectDetailIntent
    data object DeleteConfirmed : DefectDetailIntent
}