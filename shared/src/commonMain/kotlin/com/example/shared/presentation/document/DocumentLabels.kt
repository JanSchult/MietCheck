package com.example.shared.presentation.document

import com.example.shared.domain.model.DocumentType

fun DocumentType.label(): String = when (this) {
    DocumentType.UTILITY_BILL -> "Nebenkostenabrechnung"
    DocumentType.RENTAL_CONTRACT -> "Mietvertrag"
    DocumentType.HANDOVER_PROTOCOL -> "Übergabeprotokoll"
    DocumentType.DEFECT_EVIDENCE -> "Mängelnachweis"
    DocumentType.CORRESPONDENCE -> "Schriftverkehr"
    DocumentType.METER_READING -> "Zählerstand"
    DocumentType.OTHER -> "Sonstiges"
}