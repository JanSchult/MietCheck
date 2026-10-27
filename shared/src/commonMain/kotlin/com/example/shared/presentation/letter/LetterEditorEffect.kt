package com.example.shared.presentation.letter

sealed interface LetterEditorEffect {
    /** UI übernimmt Teilen/Export (Share-Sheet, PDF) – ViewModel kennt keine Plattform. */
    data class Export(val subject: String, val body: String) : LetterEditorEffect
    data class ShowError(val message: String) : LetterEditorEffect
}