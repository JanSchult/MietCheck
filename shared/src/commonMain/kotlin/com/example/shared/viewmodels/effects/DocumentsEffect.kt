package com.example.shared.viewmodels.effects

sealed interface DocumentsEffect {
    data class NavigateToDetail(val id: String) : DocumentsEffect
    data class ShowMessage(val message: String) : DocumentsEffect
}