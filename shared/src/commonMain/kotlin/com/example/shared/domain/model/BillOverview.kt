package com.example.shared.domain.model

data class BillOverview(
    val bill: UtilityBill,
    val previousBill: UtilityBill?,
    val balanceCents: Long,                 // positiv = Nachzahlung, negativ = Guthaben
    val monthlyAdvanceCents: Long?,         // laut Wohnung (Nebenkosten + Heizung)
    val costPerSqmCents: Long?,
    val previousCostPerSqmCents: Long?,
    val yearOverYearTenthPercent: Long?,    // 123 = +12,3 %
    val itemRows: List<CostItemRow>
)