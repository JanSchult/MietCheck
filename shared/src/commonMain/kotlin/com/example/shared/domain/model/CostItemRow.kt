package com.example.shared.domain.model

data class CostItemRow(
    val item: UtilityCostItem,
    val explanation: String,
    val status: ItemStatus,
    val previousAmountCents: Long?
)
