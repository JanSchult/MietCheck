package com.example.shared.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
 fun Instant.formatDate(): String {
    val d = toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${d.day.toString().padStart(2, '0')}.${d.month.number.toString().padStart(2, '0')}.${d.year}"
}

 fun parseDate(value: String): LocalDate? =
    runCatching { LocalDate.parse(value.trim()) }.getOrNull()

 fun formatSqm(sqm: Double): String =
    if (sqm % 1.0 == 0.0) sqm.toInt().toString() else sqm.toString().replace('.', ',')