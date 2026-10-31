package com.example.shared.domain.platform

/** Öffnet das System-Teilen-Menü. Es wird nichts automatisch versendet. */
interface TextSharer {
    fun shareText(subject: String, text: String)
}