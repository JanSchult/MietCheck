package com.example.shared.viewmodels.intent

import com.example.shared.domain.model.DefectStatus

sealed interface DefectsIntent {
    data object AddClicked : DefectsIntent
    data class DefectClicked(val id: String) : DefectsIntent
    data class FilterChanged(val status: DefectStatus?) : DefectsIntent
}