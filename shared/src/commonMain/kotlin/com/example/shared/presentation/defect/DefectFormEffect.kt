package com.example.shared.presentation.defect

sealed interface DefectFormEffect {
    data object Saved : DefectFormEffect
    data class ShowError(val message: String) : DefectFormEffect
}