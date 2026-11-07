package com.example.shared.domain.platform

/** Löscht alle App-Daten vollständig und beendet die App. */
expect class AppDataEraser {
    fun eraseAll()
}