package com.example.shared.presentation.lock

sealed interface LockIntent {
    data object Entered : LockIntent
    data class Digit(val value: Int) : LockIntent
    data object Backspace : LockIntent
    data object BiometricRequested : LockIntent
    data object EraseAllConfirmed : LockIntent
}