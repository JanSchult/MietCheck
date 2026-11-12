package com.example.shared.domain.usecase

import com.example.shared.domain.export.ExportRoot
import com.example.shared.domain.export.ExportSummary
import com.example.shared.domain.export.toExport
import com.example.shared.domain.model.detectMimeType
import com.example.shared.domain.model.extensionFor
import com.example.shared.domain.platform.ExportArchiveWriter
import com.example.shared.domain.platform.SecureFileStorage
import com.example.shared.domain.repository.ApartmentRepository
import com.example.shared.domain.repository.DefectRepository
import com.example.shared.domain.repository.DocumentRepository
import com.example.shared.domain.repository.ReminderRepository
import com.example.shared.domain.repository.UtilityBillRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ExportAllDataUseCase(
    private val apartmentRepository: ApartmentRepository,
    private val billRepository: UtilityBillRepository,
    private val reminderRepository: ReminderRepository,
    private val defectRepository: DefectRepository,
    private val documentRepository: DocumentRepository,
    private val storage: SecureFileStorage,
    private val writer: ExportArchiveWriter
) {
    private val json = Json { prettyPrint = true; encodeDefaults = true }

    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(): ExportSummary {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

        writer.begin("MietCheck-Export-$today.zip")
        try {
            var documentCount = 0
            var missing = 0

            val apartments = apartmentRepository.observeAll().first().map { apartment ->
                val landlord = apartment.landlordContactId?.let { apartmentRepository.getLandlord(it) }
                val bills = billRepository.getForApartment(apartment.id)
                val reminders = reminderRepository.observeForApartment(apartment.id).first()
                val defects = defectRepository.observeForApartment(apartment.id).first()

                val documents = documentRepository.observeForApartment(apartment.id).first().map { doc ->
                    documentCount++
                    // Jedes Dokument einzeln entschlüsseln und sofort ins Archiv schreiben
                    val archivePath = try {
                        val bytes = storage.read(doc.filePathLocal)
                        val ext = extensionFor(detectMimeType(bytes) ?: "")
                        val path = "dokumente/${exportName(apartment.label)}/" +
                                "${exportName(doc.displayName)}-${doc.id.value.take(8)}.$ext"
                        writer.addEntry(path, bytes)
                        path
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        null
                    }
                    if (archivePath == null) missing++
                    doc.toExport(archivePath)
                }

                apartment.toExport(landlord, bills, reminders, defects, documents)
            }

            val root = ExportRoot(
                formatVersion = 1,
                exportedAt = now.toString(),
                app = "MietCheck",
                apartments = apartments
            )
            writer.addEntry("daten.json", json.encodeToString(ExportRoot.serializer(), root).encodeToByteArray())
            writer.addEntry("LIES-MICH.txt", readme(today.toString(), missing).encodeToByteArray())
            writer.finishAndShare()

            return ExportSummary(apartments.size, documentCount, missing)
        } catch (e: Throwable) {
            writer.abort()
            throw e
        }
    }

    private fun exportName(name: String): String =
        name.map { if (it.isLetterOrDigit() || it == ' ' || it == '-' || it == '_') it else '_' }
            .joinToString("").trim().ifEmpty { "Dokument" }.take(60)

    private fun readme(date: String, missing: Int) = buildString {
        appendLine("MietCheck – Datenexport vom $date")
        appendLine()
        appendLine("Inhalt")
        appendLine("- daten.json: Wohnungen, Vermieter, Abrechnungen mit Kostenpositionen, Mängel mit Verlauf,")
        appendLine("  Fristen und die Dokumentenliste. Geldbeträge stehen in Cent.")
        appendLine("- dokumente/: deine importierten Dokumente und Scans, nach Wohnung sortiert.")
        appendLine()
        appendLine("Wichtig")
        appendLine("Dieser Export ist nicht verschlüsselt. Bewahre ihn nur an Orten auf, denen du vertraust,")
        appendLine("und lösche ihn, wenn du ihn nicht mehr brauchst.")
        appendLine("Die PIN und die App-Einstellungen sind nicht enthalten.")
        if (missing > 0) {
            appendLine()
            appendLine("Hinweis: $missing Dokument(e) konnten nicht gelesen werden und fehlen im Ordner dokumente/.")
        }
    }
}