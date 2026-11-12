package com.example.shared.domain.export

import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.LandlordContact
import com.example.shared.domain.model.Reminder
import com.example.shared.domain.model.UtilityBill
import kotlin.time.ExperimentalTime

// Eigene Export-Modelle statt der Domain-Klassen: stabiles Format, nur Primitive, keine internen Typen.
// Nicht enthalten: PIN-Daten, Schlüssel, App-Einstellungen.

internal fun UtilityBill.toExport() = ExportBill(
    id = id.value,
    periodStart = billingPeriodStart.toString(),
    periodEnd = billingPeriodEnd.toString(),
    issueDate = issueDate?.toString(),
    livingSpaceSqm = livingSpaceSqmAtBilling,
    occupants = occupantsAtBilling,
    advancePaymentsCents = advancePaymentsMadeCents,
    totalCostsCents = totalCostsCents,
    items = items.map { ExportCostItem(it.category.name, it.label, it.amountCents, it.distributionKey, it.note) }
)

@OptIn(ExperimentalTime::class)
internal fun Reminder.toExport() =
    ExportReminder(type.name, title, dueAt.toString(), recurrenceRule?.name ?:"", isCompleted)

@OptIn(ExperimentalTime::class)
internal fun Defect.toExport() = ExportDefect(
    room = room,
    description = description,
    urgency = urgency.name,
    status = status.name,
    createdAt = createdAt.toString(),
    reportedAt = reportedAt?.toString(),
    communications = communications.map {
        ExportCommunication(it.direction.name, it.summary, it.occurredAt.toString())
    }
)

@OptIn(ExperimentalTime::class)
internal fun DocumentRecord.toExport(file: String?) =
    ExportDocument(id.value, type.name, displayName, pageCount, importedAt.toString(), file)

internal fun LandlordContact.toExport() = ExportLandlord(
    name = name,
    company = company,
    email = email,
    phone = phone,
    address = postalAddress?.let { "${it.street} ${it.houseNumber}, ${it.postalCode} ${it.city}" }
)

internal fun Apartment.toExport(
    landlord: LandlordContact?,
    bills: List<UtilityBill>,
    reminders: List<Reminder>,
    defects: List<Defect>,
    documents: List<ExportDocument>
) = ExportApartment(
    id = id.value,
    label = label,
    street = address.street,
    houseNumber = address.houseNumber,
    postalCode = address.postalCode,
    city = address.city,
    livingSpaceSqm = livingSpaceSqm,
    occupants = numberOfOccupants,
    tenancyStart = tenancyStartDate.toString(),
    monthlyUtilityAdvanceCents = monthlyUtilityAdvanceCents,
    monthlyHeatingAdvanceCents = monthlyHeatingAdvanceCents,
    landlord = landlord?.toExport(),
    bills = bills.map { it.toExport() },
    reminders = reminders.map { it.toExport() },
    defects = defects.map { it.toExport() },
    documents = documents
)