package com.example.shared.domain.model

import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.LandlordContactId
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


data class Apartment @OptIn(ExperimentalTime::class) constructor(
    val id: ApartmentId,
    val label: String,
    val address: Address,
    val livingSpaceSqm: Double,
    val numberOfOccupants: Int,
    val tenancyStartDate: LocalDate,
    val monthlyUtilityAdvanceCents: Long,
    val monthlyHeatingAdvanceCents: Long,
    val landlordContactId: LandlordContactId?,
    val createdAt: Instant,
    val updatedAt: Instant
)