package com.example.shared.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportBill(
    val id: String,
    val periodStart: String,
    val periodEnd: String,
    val issueDate: String?,
    val livingSpaceSqm: Double?,
    val occupants: Int?,
    val advancePaymentsCents: Long,
    val totalCostsCents: Long,
    val items: List<ExportCostItem>
)