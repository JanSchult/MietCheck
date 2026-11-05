package com.example.shared.presentation.defect

sealed interface DefectsEffect {
    data object NavigateToForm : DefectsEffect
    data class NavigateToDetail(val id: String) : DefectsEffect
}
