package com.example.shared.viewmodels.effects

sealed interface DocumentDetailEffect {
    data object Deleted : DocumentDetailEffect
    data class ShowMessage(val message: String) : DocumentDetailEffect
}