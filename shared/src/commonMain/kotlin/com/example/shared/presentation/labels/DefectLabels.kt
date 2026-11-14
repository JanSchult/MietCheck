package com.example.shared.presentation.labels


import com.example.shared.domain.model.CommunicationDirection
import com.example.shared.domain.model.DefectStatus
import com.example.shared.domain.model.DefectUrgency
import com.example.shared.domain.model.StatusKind

fun DefectStatus.label(): String = when (this) {
    DefectStatus.NEW -> "Neu"
    DefectStatus.REPORTED -> "Gemeldet"
    DefectStatus.IN_PROGRESS -> "In Bearbeitung"
    DefectStatus.RESOLVED -> "Behoben"
}

fun DefectStatus.kind(): StatusKind =
    if (this == DefectStatus.RESOLVED) StatusKind.OK else StatusKind.NOTE

fun DefectUrgency.label(): String = when (this) {
    DefectUrgency.LOW -> "Niedrig"
    DefectUrgency.MEDIUM -> "Mittel"
    DefectUrgency.HIGH -> "Hoch"
}

fun CommunicationDirection.label(): String = when (this) {
    CommunicationDirection.OUTGOING -> "Von mir an den Vermieter"
    CommunicationDirection.INCOMING -> "Vom Vermieter an mich"
}