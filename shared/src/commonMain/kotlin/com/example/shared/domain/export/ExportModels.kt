package com.example.shared.domain.export

import com.example.shared.domain.model.Apartment
import com.example.shared.domain.model.Defect
import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.LandlordContact
import com.example.shared.domain.model.Reminder
import com.example.shared.domain.model.UtilityBill
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

// Eigene Export-Modelle statt der Domain-Klassen: stabiles Format, nur Primitive, keine internen Typen.
// Nicht enthalten: PIN-Daten, Schlüssel, App-Einstellungen.

@Serializable
data class ExportRoot(
    val formatVersion: Int,
    val exportedAt: String,
    val app: String,
    val apartments: List<ExportApartment>
)

@Serializable
data class ExportApartment(
    val id: String,
    val label: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val livingSpaceSqm: Double,
    val occupants: Int,
    val tenancyStart: String,
    val monthlyUtilityAdvanceCents: Long,
    val monthlyHeatingAdvanceCents: Long,
    val landlord: ExportLandlord?,
    val bills: List<ExportBill>,
    val reminders: List<ExportReminder>,
    val defects: List<ExportDefect>,
    val documents: List<ExportDocument>
)

@Serializable
data class ExportLandlord(
    val name: String,
    val company: String?,
    val email: String?,
    val phone: String?,
    val address: String?
)

@Serializable
data class ExportBill(
    val id: String,
    val periodStart: String,
    val periodEnd: String,
    val issueDate: String?,
    val livingSpaceSqm: Double?,
    val occupants: Int?,
    val advancePaymentsCents: Long,
    val totalCostsCents: Long,
    val items: List<ExportCostItem>
)

@Serializable
data class ExportCostItem(
    val category: String,
    val label: String,
    val amountCents: Long,
    val distributionKey: String?,
    val note: String?
)

@Serializable
data class ExportReminder(
    val type: String,
    val title: String,
    val dueAt: String,
    val recurrence: String,
    val completed: Boolean
)

@Serializable
data class ExportDefect(
    val room: String,
    val description: String,
    val urgency: String,
    val status: String,
    val createdAt: String,
    val reportedAt: String?,
    val communications: List<ExportCommunication>
)

@Serializable
data class ExportCommunication(val direction: String, val summary: String, val occurredAt: String)

@Serializable
data class ExportDocument(
    val id: String,
    val type: String,
    val name: String,
    val pages: Int,
    val importedAt: String,
    val file: String?          // Pfad im Archiv, null = Datei konnte nicht gelesen werden
)

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