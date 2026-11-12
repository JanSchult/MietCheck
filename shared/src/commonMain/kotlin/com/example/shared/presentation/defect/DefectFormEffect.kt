package com.example.shared.presentation.defect

sealed interface DefectFormEffect {
    data class Saved(val defectId: String) : DefectFormEffect
    data class ShowError(val message: String) : DefectFormEffect
}