package com.example.shared.presentation.defect

sealed interface DefectDetailEffect {
    data object Deleted : DefectDetailEffect
    data class ShowError(val message: String) : DefectDetailEffect
}