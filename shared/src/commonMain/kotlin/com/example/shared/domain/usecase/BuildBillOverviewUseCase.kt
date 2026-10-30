package com.example.shared.domain.usecase

import com.example.shared.domain.model.BillOverview
import com.example.shared.domain.model.CostCategory
import com.example.shared.domain.model.CostItemRow
import com.example.shared.domain.model.ItemStatus
import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.ids.UtilityBillId
import com.example.shared.domain.repository.ApartmentRepository
import com.example.shared.domain.repository.UtilityBillRepository
import kotlin.math.abs
import kotlin.math.roundToLong

class BuildBillOverviewUseCase(
    private val billRepository: UtilityBillRepository,
    private val apartmentRepository: ApartmentRepository
) {
    suspend operator fun invoke(billId: UtilityBillId): BillOverview? {
        val bill = billRepository.getById(billId) ?: return null
        val previous = billRepository.getPreviousYear(bill.apartmentId, bill.billingPeriodEnd)
        val apartment = apartmentRepository.getById(bill.apartmentId)

        return BillOverview(
            bill = bill,
            previousBill = previous,
            balanceCents = bill.totalCostsCents - bill.advancePaymentsMadeCents,
            monthlyAdvanceCents = apartment?.let { it.monthlyUtilityAdvanceCents + it.monthlyHeatingAdvanceCents },
            costPerSqmCents = perSqmCents(bill.totalCostsCents, bill.livingSpaceSqmAtBilling),
            previousCostPerSqmCents = previous?.let { perSqmCents(it.totalCostsCents, it.livingSpaceSqmAtBilling) },
            yearOverYearTenthPercent = previous?.let { tenthPercent(bill.totalCostsCents, it.totalCostsCents) },
            itemRows = buildRows(bill, previous)
        )
    }

    private fun buildRows(bill: UtilityBill, previous: UtilityBill?): List<CostItemRow> {
        val anyKey = bill.items.any { it.distributionKey != null }
        val currentByCategory = bill.items.groupBy { it.category }
        val previousSums = previous?.items
            ?.groupBy { it.category }
            ?.mapValues { (_, items) -> items.sumOf { it.amountCents } }
            .orEmpty()

        return bill.items.map { item ->
            // Vorjahresvergleich nur eindeutig, wenn die Kategorie genau einmal vorkommt
            val previousAmount = if (currentByCategory.getValue(item.category).size == 1) {
                previousSums[item.category]
            } else null

            val deviates = previousAmount != null && previousAmount > 0 && run {
                val diff = abs(item.amountCents - previousAmount)
                diff >= MIN_DEVIATION_CENTS && diff * 100 > previousAmount * YOY_THRESHOLD_PERCENT
            }

            val status = when {
                item.category in REVIEW_CATEGORIES -> ItemStatus.MANUAL_REVIEW
                deviates -> ItemStatus.YOY_DEVIATION
                anyKey && item.distributionKey == null -> ItemStatus.MISSING_INFO
                else -> ItemStatus.NO_FINDING
            }

            CostItemRow(
                item = item,
                explanation = CostItemExplanations.forCategory(item.category),
                status = status,
                previousAmountCents = previousAmount
            )
        }
    }

    private fun perSqmCents(totalCents: Long, sqm: Double?): Long? {
        val hundredths = sqm?.let { (it * 100).roundToLong() } ?: return null
        if (hundredths <= 0) return null
        return (totalCents * 100 + hundredths / 2) / hundredths   // kaufmännisch gerundet
    }

    private fun tenthPercent(current: Long, previous: Long): Long? {
        if (previous <= 0) return null
        val scaled = (current - previous) * 1000
        return if (scaled >= 0) (scaled + previous / 2) / previous
        else -((-scaled + previous / 2) / previous)
    }

    private companion object {
        const val YOY_THRESHOLD_PERCENT = 30L
        const val MIN_DEVIATION_CENTS = 2_000L   // unter 20 € Differenz kein Hinweis
        val REVIEW_CATEGORIES = setOf(CostCategory.REPAIRS, CostCategory.ADMINISTRATION)
    }
}