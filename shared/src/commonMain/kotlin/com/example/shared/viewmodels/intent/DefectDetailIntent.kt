package com.example.shared.viewmodels.intent

import com.example.shared.domain.model.CommunicationDirection
import com.example.shared.domain.model.DefectStatus
import com.example.shared.domain.model.StoredMedia

sealed interface DefectDetailIntent {
    data class Load(val defectId: String) : DefectDetailIntent
    data class StatusSelected(val status: DefectStatus) : DefectDetailIntent
    data class AddCommunication(val direction: CommunicationDirection, val summary: String) : DefectDetailIntent
    data object DeleteConfirmed : DefectDetailIntent
    data class MediaStored(val media: StoredMedia) : DefectDetailIntent
    data class RemoveMedia(val storedName: String) : DefectDetailIntent
    data class OpenMedia(val storedName: String, val share: Boolean) : DefectDetailIntent

}