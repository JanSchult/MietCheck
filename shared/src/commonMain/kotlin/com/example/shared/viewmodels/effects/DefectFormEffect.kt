package com.example.shared.viewmodels.effects

sealed interface DefectFormEffect {
    data class Saved(val defectId: String) : DefectFormEffect
    data class ShowError(val message: String) : DefectFormEffect
}