package com.example.shared.viewmodels.state

import com.example.shared.domain.model.DefectUrgency


const val OTHER_ROOM = "Sonstiger Raum"
val DEFECT_ROOMS = listOf(
    "Wohnzimmer", "Schlafzimmer", "Kinderzimmer", "Küche", "Bad", "WC",
    "Flur", "Balkon / Terrasse", "Keller", OTHER_ROOM
)

data class DefectFormState(
    val room: String = DEFECT_ROOMS.first(),
    val customRoom: String = "",
    val description: String = "",
    val urgency: DefectUrgency = DefectUrgency.MEDIUM,
    val roomError: String? = null,
    val descriptionError: String? = null,
    val isSaving: Boolean = false
)
