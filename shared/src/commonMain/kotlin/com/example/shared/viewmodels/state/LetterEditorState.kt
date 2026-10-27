package com.example.shared.viewmodels.state

data class LetterEditorState(
    val subject: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
    val isConfirmed: Boolean = false,
    val hasOpenPlaceholders: Boolean = false,
    val error: String? = null
)
