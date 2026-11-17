package com.example.shared.domain.model

import com.example.shared.domain.model.enums.CostCategory

data class CostItemInput(
    val key: Long,                       // nur für stabile Listen-Keys in der UI
    val category: CostCategory = CostCategory.OTHER,
    val label: String = "",
    val amount: String = "",
    val distributionKey: String = ""
)