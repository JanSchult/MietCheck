package com.example.shared.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportLandlord(
    val name: String,
    val company: String?,
    val email: String?,
    val phone: String?,
    val address: String?
)