package com.example.shared.domain.model

enum class CommunicationDirection {
    /** Eingehende Kommunikation (z. B. Nachfragen oder Meldungen vom Mieter/Handwerker) */
    INCOMING,

    /** Ausgehende Kommunikation (z. B. Briefe, Abrechnungen oder E-Mails an Mieter/Handwerker) */
    OUTGOING
}