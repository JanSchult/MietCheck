package com.example.shared.viewmodels.effects

sealed interface DefectsEffect {
    data object NavigateToForm : DefectsEffect
    data class NavigateToDetail(val id: String) : DefectsEffect
}
