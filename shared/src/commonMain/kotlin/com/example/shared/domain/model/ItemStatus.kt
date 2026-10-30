package com.example.shared.domain.model

enum class ItemStatus {
    NO_FINDING,        // Keine Auffälligkeit erkannt
    MANUAL_REVIEW,     // Manuelle Prüfung empfohlen
    MISSING_INFO,      // Angabe fehlt
    YOY_DEVIATION,     // Wert weicht stark vom Vorjahr ab
    LEGAL_REVIEW       // Rechtliche Prüfung erforderlich (reserviert, wird noch nicht vergeben)
}