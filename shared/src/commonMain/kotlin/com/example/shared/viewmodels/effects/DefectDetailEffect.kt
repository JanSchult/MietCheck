package com.example.shared.viewmodels.effects

sealed interface DefectDetailEffect {
    data object Deleted : DefectDetailEffect
    data class ShowError(val message: String) : DefectDetailEffect
}