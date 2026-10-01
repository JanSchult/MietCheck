package com.example.shared.domain.usecase

import com.example.shared.domain.model.UtilityBill

object BillingCalculator {

    data class BillingResult(
        val totalCostsCents: Long,
        val advancePaymentsCents: Long,
        val balanceCents: Long,       // positiv = Nachzahlung, negativ = Guthaben
        val isRefund: Boolean,
        val costPerSqmCents: Long?    // Cent pro m², nur wenn Wohnfläche bekannt
    )

    fun calculate(bill: UtilityBill): BillingResult {
        val balance = bill.totalCostsCents - bill.advancePaymentsMadeCents
        val costPerSqm = bill.livingSpaceSqmAtBilling
            ?.takeIf { it > BigDecimal.ZERO }
            ?.let { sqm ->
                (BigDecimal(bill.totalCostsCents).divide(sqm, 0, RoundingMode.HALF_UP)).toLong()
            }
        return BillingResult(
            totalCostsCents = bill.totalCostsCents,
            advancePaymentsCents = bill.advancePaymentsMadeCents,
            balanceCents = balance,
            isRefund = balance < 0,
            costPerSqmCents = costPerSqm
        )
    }

    fun yearOverYearDeviationPercent(current: UtilityBill, previous: UtilityBill?): BigDecimal? {
        if (previous == null || previous.totalCostsCents == 0L) return null
        val diff = current.totalCostsCents - previous.totalCostsCents
        return BigDecimal(diff)
            .divide(BigDecimal(previous.totalCostsCents), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
            .setScale(1, RoundingMode.HALF_UP)
    }

    fun sumOfItemsCents(bill: UtilityBill): Long = bill.items.sumOf { it.amountCents }

    fun itemsVsTotalDifferenceCents(bill: UtilityBill): Long =
        bill.totalCostsCents - sumOfItemsCents(bill)
}