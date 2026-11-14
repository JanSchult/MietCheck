package com.example.shared.domain.model


data class TenantProfile(
    val name: String = "",
    val email: String = "",
    val phone: String = ""
) {
    val isEmpty: Boolean get() = name.isBlank() && email.isBlank() && phone.isBlank()

    /** Hängt vorhandene Kontaktdaten unter den Adressblock des Absenders. */
    fun withContact(addressBlock: String): String = buildList {
        add(addressBlock)
        if (phone.isNotBlank()) add("Tel.: ${phone.trim()}")
        if (email.isNotBlank()) add("E-Mail: ${email.trim()}")
    }.joinToString("\n")
}