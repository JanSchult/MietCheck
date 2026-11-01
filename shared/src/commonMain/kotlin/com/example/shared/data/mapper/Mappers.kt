package com.example.shared.data.mapper


import com.example.shared.data.local.ApartmentEntity
import com.example.shared.data.local.CostItemEntity
import com.example.shared.data.local.DocumentEntity
import com.example.shared.data.local.LandlordEntity
import com.example.shared.data.local.ReminderEntity
import com.example.shared.data.local.UtilityBillEntity
import com.example.shared.domain.model.Address
import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.CostCategory
import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.DocumentType
import com.example.shared.domain.model.LandlordContact
import com.example.shared.domain.model.RecurrenceRule
import com.example.shared.domain.model.Reminder
import com.example.shared.domain.model.ReminderType
import com.example.shared.domain.model.UtilityBill
import com.example.shared.domain.model.UtilityCostItem
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.CostItemId
import com.example.shared.domain.model.ids.DocumentId
import com.example.shared.domain.model.ids.LandlordContactId
import com.example.shared.domain.model.ids.ReminderId
import com.example.shared.domain.model.ids.UtilityBillId
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Unbekannter Enum-Name (z. B. nach einem Downgrade) fällt auf einen Default zurück statt zu crashen. */
private inline fun <reified E : Enum<E>> enumOrDefault(name: String, default: E): E =
    enumValues<E>().firstOrNull { it.name == name } ?: default

// ---------- Apartment / Landlord ----------

@OptIn(ExperimentalTime::class)
internal fun ApartmentEntity.toDomain() = Apartment(
    id = ApartmentId(id),
    label = label,
    address = Address(street, houseNumber, postalCode, city),
    livingSpaceSqm = livingSpaceSqm,
    numberOfOccupants = occupants.toInt(),
    tenancyStartDate = LocalDate.parse(tenancyStart),
    monthlyUtilityAdvanceCents = monthlyUtilityAdvanceCents,
    monthlyHeatingAdvanceCents = monthlyHeatingAdvanceCents,
    landlordContactId = landlordContactId?.let { LandlordContactId(it) },
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt)
)

@OptIn(ExperimentalTime::class)
internal fun Apartment.toEntity() = ApartmentEntity(
    id = id.value,
    label = label,
    street = address.street,
    houseNumber = address.houseNumber,
    postalCode = address.postalCode,
    city = address.city,
    livingSpaceSqm = livingSpaceSqm,
    occupants = numberOfOccupants.toLong(),
    tenancyStart = tenancyStartDate.toString(),
    monthlyUtilityAdvanceCents = monthlyUtilityAdvanceCents,
    monthlyHeatingAdvanceCents = monthlyHeatingAdvanceCents,
    landlordContactId = landlordContactId?.value,
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds()
)

internal fun LandlordEntity.toDomain(): LandlordContact {
    val address = if (street != null && houseNumber != null && postalCode != null && city != null) {
        Address(street, houseNumber, postalCode, city)
    } else null
    return LandlordContact(
        id = LandlordContactId(id),
        name = name,
        company = company,
        email = email,
        phone = phone,
        postalAddress = address
    )
}

internal fun LandlordContact.toEntity() = LandlordEntity(
    id = id.value,
    name = name,
    company = company,
    email = email,
    phone = phone,
    street = postalAddress?.street,
    houseNumber = postalAddress?.houseNumber,
    postalCode = postalAddress?.postalCode,
    city = postalAddress?.city
)

// ---------- UtilityBill ----------

@OptIn(ExperimentalTime::class)
internal fun UtilityBillEntity.toDomain(items: List<CostItemEntity>) = UtilityBill(
    id = UtilityBillId(id),
    apartmentId = ApartmentId(apartmentId),
    documentId = documentId?.let { DocumentId(it) },
    billingPeriodStart = LocalDate.parse(periodStart),
    billingPeriodEnd = LocalDate.parse(periodEnd),
    issueDate = issueDate?.let { LocalDate.parse(it) },
    livingSpaceSqmAtBilling = livingSpaceSqm,
    occupantsAtBilling = occupants?.toInt(),
    advancePaymentsMadeCents = advancePaymentsCents,
    totalCostsCents = totalCostsCents,
    distributionKeyNoted = distributionKeyNoted,
    items = items.map { it.toDomain() },
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt)
)

@OptIn(ExperimentalTime::class)
internal fun UtilityBill.toEntity() = UtilityBillEntity(
    id = id.value,
    apartmentId = apartmentId.value,
    documentId = documentId?.value,
    periodStart = billingPeriodStart.toString(),
    periodEnd = billingPeriodEnd.toString(),
    issueDate = issueDate?.toString(),
    livingSpaceSqm = livingSpaceSqmAtBilling,
    occupants = occupantsAtBilling?.toLong(),
    advancePaymentsCents = advancePaymentsMadeCents,
    totalCostsCents = totalCostsCents,
    distributionKeyNoted = distributionKeyNoted,
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds()
)

internal fun CostItemEntity.toDomain() = UtilityCostItem(
    id = CostItemId(id),
    category = enumOrDefault(category, CostCategory.OTHER),
    label = label,
    amountCents = amountCents,
    distributionKey = distributionKey,
    note = note
)

internal fun UtilityCostItem.toEntity(billId: String, position: Int) = CostItemEntity(
    id = id.value,
    billId = billId,
    position = position.toLong(),
    category = category.name,
    label = label,
    amountCents = amountCents,
    distributionKey = distributionKey,
    note = note
)

// ---------- Reminder ----------

@OptIn(ExperimentalTime::class)
internal fun ReminderEntity.toDomain() = Reminder(
    id = ReminderId(id),
    apartmentId = ApartmentId(apartmentId),
    type = enumOrDefault(type, ReminderType.SELF_SET_DEADLINE),
    title = title,
    dueAt = Instant.fromEpochMilliseconds(dueAt),
    recurrenceRule = enumOrDefault(recurrence, RecurrenceRule.NONE),
    isCompleted = isCompleted,
    linkedEntityId = linkedEntityId
)

@OptIn(ExperimentalTime::class)
internal fun Reminder.toEntity() = ReminderEntity(
    id = id.value,
    apartmentId = apartmentId.value,
    type = type.name,
    title = title,
    dueAt = dueAt.toEpochMilliseconds(),
    recurrence = recurrenceRule?.name ?: "",
    isCompleted = isCompleted,
    linkedEntityId = linkedEntityId
)

// ---------- Document ----------

@OptIn(ExperimentalTime::class)
internal fun DocumentEntity.toDomain() = DocumentRecord(
    id = DocumentId(id),
    apartmentId = ApartmentId(apartmentId),
    type = enumOrDefault(type, DocumentType.OTHER),
    displayName = displayName,
    filePathLocal = filePathLocal,
    pageCount = pageCount.toInt(),
    importedAt = Instant.fromEpochMilliseconds(importedAt),
    sourceHash = sourceHash
)

@OptIn(ExperimentalTime::class)
internal fun DocumentRecord.toEntity() = DocumentEntity(
    id = id.value,
    apartmentId = apartmentId.value,
    type = type.name,
    displayName = displayName,
    filePathLocal = filePathLocal,
    pageCount = pageCount.toLong(),
    importedAt = importedAt.toEpochMilliseconds(),
    sourceHash = sourceHash
)