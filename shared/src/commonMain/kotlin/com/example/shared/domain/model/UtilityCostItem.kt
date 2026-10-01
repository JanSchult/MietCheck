package com.example.shared.domain.model

import com.example.shared.model.ids.CostItemId

data class UtilityCostItem(
    val id: CostItemId,
    val category: CostCategory,
    val label: String,
    val amountCents: Long,
    val distributionKey: String?,
    val note: String?
)