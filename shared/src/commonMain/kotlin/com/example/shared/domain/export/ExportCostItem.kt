package com.example.shared.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportCostItem(
    val category: String,
    val label: String,
    val amountCents: Long,
    val distributionKey: String?,
    val note: String?
)