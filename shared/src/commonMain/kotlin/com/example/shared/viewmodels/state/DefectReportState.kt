package com.example.shared.viewmodels.state

data class DefectReportState(
    val subject: String = "",
    val body: String = "",
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isConfirmed: Boolean = false,
    val hasOpenPlaceholders: Boolean = false,
    val isWorking: Boolean = false
)