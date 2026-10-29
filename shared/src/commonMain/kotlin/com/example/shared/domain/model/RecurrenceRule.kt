package com.example.shared.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class RecurrenceRule { NONE, MONTHLY, YEARLY }

/** Nächster Termin nach [current], oder null bei NONE. */
@OptIn(ExperimentalTime::class)
fun RecurrenceRule.nextAfter(current: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): Instant? =
    when (this) {
        RecurrenceRule.NONE -> null
        RecurrenceRule.MONTHLY -> current.plus(1, DateTimeUnit.MONTH, zone)
        RecurrenceRule.YEARLY -> current.plus(1, DateTimeUnit.YEAR, zone)
    }