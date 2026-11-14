package com.example.shared.domain.usecase

import com.example.shared.domain.model.CommunicationDirection
import com.example.shared.domain.model.DefectReportDraft
import com.example.shared.domain.model.DefectUrgency
import com.example.shared.domain.model.LandlordContact
import com.example.shared.domain.model.ids.DefectId
import com.example.shared.domain.repository.ApartmentRepository
import com.example.shared.domain.repository.DefectRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Stellt eine neutrale Mängelanzeige aus den gespeicherten Daten zusammen.
 * Datum und Unterschrift bleiben als Platzhalter stehen, fehlende Angaben ebenfalls.
 * Gibt null zurück, wenn der Mangel nicht existiert.
 */
class BuildDefectReportUseCase(
    private val defectRepository: DefectRepository,
    private val apartmentRepository: ApartmentRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(defectId: DefectId): DefectReportDraft? {
        val defect = defectRepository.getById(defectId) ?: return null
        val apartment = apartmentRepository.getById(defect.apartmentId)

        val landlordId = apartment?.landlordContactId
        val landlord: LandlordContact? =
            if (landlordId != null) apartmentRepository.getLandlord(landlordId) else null

        val addressBlock: String? = apartment?.address?.let { "${it.street} ${it.houseNumber}\n${it.postalCode} ${it.city}" }
        val addressLine: String = addressBlock?.replace("\n", ", ") ?: "[Adresse der Wohnung]"

        val recipientName: String = if (landlord != null) {
            listOfNotNull(landlord.company, landlord.name).joinToString("\n")
        } else "[Vermieter / Hausverwaltung]"
        val recipientAddress: String = landlord?.postalAddress
            ?.let { "${it.street} ${it.houseNumber}\n${it.postalCode} ${it.city}" }
            ?: "[Adresse Vermieter]"

        val subject = "Mängelanzeige – ${defect.room}, $addressLine"
        val history = defect.communications.sortedBy { it.occurredAt }
        val mediaCount = defect.mediaFilePaths.size

        val body = buildString {
            appendLine("[Dein Name]")
            appendLine(addressBlock ?: "[Deine Adresse]")
            appendLine()
            appendLine(recipientName)
            appendLine(recipientAddress)
            appendLine()
            appendLine("{{date}}")
            appendLine()
            appendLine("Betreff: $subject")
            appendLine()
            appendLine("Sehr geehrte Damen und Herren,")
            appendLine()
            appendLine("ich möchte Ihnen folgenden Mangel in meiner Wohnung ($addressLine) mitteilen:")
            appendLine()
            appendLine("Raum: ${defect.room}")
            appendLine("Erfasst am: ${formatDate(defect.createdAt)}")
            appendLine("Dringlichkeit (meine Einschätzung): ${urgencyText(defect.urgency)}")
            appendLine()
            appendLine("Beschreibung:")
            appendLine(defect.description)
            appendLine()
            if (history.isNotEmpty()) {
                appendLine("Bisherige Kommunikation zu diesem Mangel:")
                history.forEach {
                    appendLine("- ${formatDate(it.occurredAt)} (${directionText(it.direction)}): ${it.summary}")
                }
                appendLine()
            }
            if (mediaCount > 0) {
                appendLine(
                    "Zu dem Mangel liegen mir $mediaCount Foto(s) oder Video(s) vor, die ich Ihnen " +
                            "auf Wunsch gern zur Verfügung stelle."
                )
                appendLine()
            }
            appendLine("Bitte teilen Sie mir mit, wie und wann der Mangel behoben werden kann.")
            appendLine()
            appendLine("Vielen Dank für Ihre Rückmeldung und freundliche Grüße")
            appendLine()
            append("{{signature}}")
        }

        return DefectReportDraft(subject, body)
    }

    @OptIn(ExperimentalTime::class)
    private fun formatDate(instant: Instant): String {
        val d = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "${d.day.toString().padStart(2, '0')}.${d.month.number.toString().padStart(2, '0')}.${d.year}"
    }

    private fun urgencyText(urgency: DefectUrgency) = when (urgency) {
        DefectUrgency.LOW -> "niedrig"
        DefectUrgency.MEDIUM -> "mittel"
        DefectUrgency.HIGH -> "hoch"
    }

    private fun directionText(direction: CommunicationDirection) = when (direction) {
        CommunicationDirection.OUTGOING -> "von mir"
        CommunicationDirection.INCOMING -> "von Ihnen"
    }
}