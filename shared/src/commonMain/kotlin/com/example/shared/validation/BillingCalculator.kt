package com.example.shared.domain.usecase

import com.example.shared.domain.model.UtilityBill
import kotlin.math.round

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
            ?.takeIf { it > 0.0 }
            ?.let { sqm ->
                (bill.totalCostsCents / sqm).toLong()
            }
        return BillingResult(
            totalCostsCents = bill.totalCostsCents,
            advancePaymentsCents = bill.advancePaymentsMadeCents,
            balanceCents = balance,
            isRefund = balance < 0,
            costPerSqmCents = costPerSqm
        )
    }

    fun yearOverYearDeviationPercent(current: UtilityBill, previous: UtilityBill?): Double? {
        if (previous == null || previous.totalCostsCents == 0L) return null
        val diff = current.totalCostsCents - previous.totalCostsCents
        val percent = (diff.toDouble() * 100.0) / previous.totalCostsCents.toDouble()
        // Auf 1 Nachkommastelle runden (analog zu vorher mit BigDecimal)
        return round(percent * 10.0) / 10.0
    }

    fun sumOfItemsCents(bill: UtilityBill): Long = bill.items.sumOf { it.amountCents }

    fun itemsVsTotalDifferenceCents(bill: UtilityBill): Long =
        bill.totalCostsCents - sumOfItemsCents(bill)

}

