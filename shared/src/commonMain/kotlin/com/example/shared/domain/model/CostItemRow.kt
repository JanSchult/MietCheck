package com.example.shared.domain.model

import com.example.shared.domain.model.enums.ItemStatus

data class CostItemRow(
    val item: UtilityCostItem,
    val explanation: String,
    val status: ItemStatus,
    val previousAmountCents: Long?
)
