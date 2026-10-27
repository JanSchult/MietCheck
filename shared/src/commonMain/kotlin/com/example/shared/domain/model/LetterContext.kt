package com.example.shared.domain.model

data class LetterContext(
    val senderName: String,
    val senderAddress: String,
    val recipientName: String,
    val recipientAddress: String,
    val apartmentAddress: String,
    val billingPeriod: String,      // z. B. "01.01.2025 – 31.12.2025"
    val concreteQuestion: String    // vom Nutzer ergänzte Frage, darf leer sein
)