package com.example.shared.presentation.reminder

import com.example.shared.domain.model.RecurrenceRule
import com.example.shared.domain.model.ReminderType

fun ReminderType.displayName(): String = when (this) {
    ReminderType.BILL_DUE -> "Abrechnung erwartet"
    ReminderType.RECEIPT_REVIEW -> "Belege prüfen"
    ReminderType.FOLLOW_UP -> "Rückfrage nachfassen"
    ReminderType.SELF_SET_DEADLINE -> "Eigene Frist"
    ReminderType.METER_READING_RECURRING -> "Zählerstand ablesen"
}

fun RecurrenceRule.displayName(): String = when (this) {
    RecurrenceRule.NONE -> "Einmalig"
    RecurrenceRule.MONTHLY -> "Monatlich"
    RecurrenceRule.YEARLY -> "Jährlich"
}