package com.example.shared.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportRoot(
    val formatVersion: Int,
    val exportedAt: String,
    val app: String,
    val tenant: ExportTenant?,
    val apartments: List<ExportApartment>
)