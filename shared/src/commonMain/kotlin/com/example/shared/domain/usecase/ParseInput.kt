package com.example.shared.domain.usecase

/**
 * "1.234,56" / "1234,56" / "1234.56" / "1234" -> Cent. Kein Double im Spiel.
 * Gibt null zurück bei ungültiger Eingabe.
 */
fun parseEuroToCents(input: String): Long? {
    val cleaned = input.trim().replace("€", "").replace(" ", "")
    if (cleaned.isEmpty()) return null

    // Deutsches Format: Punkt = Tausender, Komma = Dezimal
    val normalized = if (cleaned.contains(',')) {
        cleaned.replace(".", "").replace(',', '.')
    } else cleaned

    val parts = normalized.split('.')
    if (parts.size > 2) return null
    val euros = parts[0].toLongOrNull() ?: return null
    if (euros < 0) return null
    val centsPart = when (parts.getOrNull(1)) {
        null -> 0L
        else -> {
            val frac = parts[1]
            if (frac.length > 2 || frac.any { !it.isDigit() }) return null
            frac.padEnd(2, '0').toLong()
        }
    }
    return euros * 100 + centsPart
}

fun parseSqm(input: String): Double? =
    input.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }