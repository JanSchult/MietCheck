package com.example.shared.presentation.letter

sealed interface LetterEditorIntent {
    data class Load(val billId: String, val templateTypeName: String) : LetterEditorIntent
    data class SubjectChanged(val value: String) : LetterEditorIntent
    data class BodyChanged(val value: String) : LetterEditorIntent
    data object ConfirmAndExport : LetterEditorIntent
}