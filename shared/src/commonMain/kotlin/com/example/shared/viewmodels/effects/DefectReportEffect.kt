package com.example.shared.viewmodels.effects

sealed interface DefectReportEffect {
    data class ShowMessage(val message: String) : DefectReportEffect
}
