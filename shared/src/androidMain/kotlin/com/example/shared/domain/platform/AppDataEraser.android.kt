package com.example.shared.domain.platform

import android.content.Context
import android.app.ActivityManager
actual class AppDataEraser(private val context: Context) {
    actual fun eraseAll() {
        // Entfernt Datenbank, Dateien und Einstellungen und beendet den Prozess
        (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
    }
}