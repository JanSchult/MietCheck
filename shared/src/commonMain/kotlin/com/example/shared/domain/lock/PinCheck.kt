package com.example.shared.domain.lock

sealed interface PinCheck {
    data object Correct : PinCheck
    data class Wrong(val attemptsLeftBeforeDelay: Int) : PinCheck
    data class LockedOut(val seconds: Long) : PinCheck
}
fun isValidPin(pin: String): Boolean = pin.length == PIN_LENGTH && pin.all { it in '0'..'9' }

/** Blockt triviale PINs wie 000000 oder 123456. */
fun isWeakPin(pin: String): Boolean {
    if (pin.all { it == pin.first() }) return true
    val diffs = pin.zipWithNext { a, b -> b - a }
    return diffs.all { it == 1 } || diffs.all { it == -1 }
}