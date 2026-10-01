package com.example.shared.domain.model

import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.DocumentId
import com.example.shared.domain.model.ids.UtilityBillId
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class UtilityBill @OptIn(ExperimentalTime::class) constructor(
    val id: UtilityBillId,
    val apartmentId: ApartmentId,
    val documentId: DocumentId?,
    val billingPeriodStart: LocalDate,
    val billingPeriodEnd: LocalDate,
    val issueDate: LocalDate?,
    val livingSpaceSqmAtBilling: Double?,
    val occupantsAtBilling: Int?,
    val advancePaymentsMadeCents: Long,
    val totalCostsCents: Long,
    val distributionKeyNoted: Boolean,
    val items: List<UtilityCostItem>,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val differenceCents: Long get() = totalCostsCents - advancePaymentsMadeCents
    // positiv = Nachzahlung, negativ = Guthaben
}