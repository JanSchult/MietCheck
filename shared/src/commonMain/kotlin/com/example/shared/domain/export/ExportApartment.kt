package com.example.shared.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportApartment(
    val id: String,
    val label: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val livingSpaceSqm: Double,
    val occupants: Int,
    val tenancyStart: String,
    val monthlyUtilityAdvanceCents: Long,
    val monthlyHeatingAdvanceCents: Long,
    val landlord: ExportLandlord?,
    val bills: List<ExportBill>,
    val reminders: List<ExportReminder>,
    val defects: List<ExportDefect>,
    val documents: List<ExportDocument>
)