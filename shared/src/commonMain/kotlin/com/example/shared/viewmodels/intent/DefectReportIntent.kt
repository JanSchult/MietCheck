package com.example.shared.viewmodels.intent

sealed interface DefectReportIntent {
    data class Load(val defectId: String) : DefectReportIntent
    data class SubjectChanged(val value: String) : DefectReportIntent
    data class BodyChanged(val value: String) : DefectReportIntent
    data class ConfirmedChanged(val value: Boolean) : DefectReportIntent
    data object ShareText : DefectReportIntent
    data object SharePdf : DefectReportIntent
}