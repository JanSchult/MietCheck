package com.example.shared.domain.usecase


import com.example.shared.domain.model.DocumentRecord
import com.example.shared.domain.model.DocumentType
import com.example.shared.domain.model.ImportedFile
import com.example.shared.domain.model.MAX_IMPORT_BYTES
import com.example.shared.domain.model.detectMimeType
import com.example.shared.domain.model.extensionFor
import com.example.shared.domain.model.ids.ApartmentId
import com.example.shared.domain.model.ids.DocumentId
import com.example.shared.domain.platform.FileSharer
import com.example.shared.domain.platform.SecureFileStorage
import com.example.shared.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ObserveDocumentsUseCase(private val repository: DocumentRepository) {
    operator fun invoke(apartmentId: ApartmentId): Flow<List<DocumentRecord>> =
        repository.observeForApartment(apartmentId)
}

class GetDocumentUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(id: DocumentId): DocumentRecord? = repository.getById(id)
}

sealed interface ImportResult {
    data class Saved(val id: DocumentId) : ImportResult
    data class Duplicate(val existingName: String) : ImportResult
}

class ImportDocumentUseCase(
    private val storage: SecureFileStorage,
    private val repository: DocumentRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(apartmentId: ApartmentId, type: DocumentType, file: ImportedFile): ImportResult {
        // Prüfung hier nochmals, dem Aufrufer wird nicht vertraut
        require(file.bytes.size.toLong() <= MAX_IMPORT_BYTES) { "Die Datei ist größer als 25 MB." }
        require(detectMimeType(file.bytes) != null) {
            "Dieses Dateiformat wird nicht unterstützt. Erlaubt sind PDF, JPEG und PNG."
        }

        val duplicate = repository.observeForApartment(apartmentId).first()
            .firstOrNull { it.sourceHash == file.sha256 }
        if (duplicate != null) return ImportResult.Duplicate(duplicate.displayName)

        val id = DocumentId.new()
        val storedName = storage.save(file.bytes, "${id.value}.bin")
        val record = DocumentRecord(
            id = id,
            apartmentId = apartmentId,
            type = type,
            displayName = file.suggestedName.trim().ifBlank { "Dokument" }.take(80),
            filePathLocal = storedName,
            pageCount = file.pageCount.coerceAtLeast(1),
            importedAt = Clock.System.now(),
            sourceHash = file.sha256
        )
        try {
            repository.save(record)
        } catch (e: Throwable) {
            storage.delete(storedName)      // keine verwaiste Datei zurücklassen
            throw e
        }
        return ImportResult.Saved(id)
    }
}

class UpdateDocumentUseCase(private val repository: DocumentRepository) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(
        id: DocumentId,
        name: String,
        type: DocumentType,
        apartmentId: ApartmentId
    ): DocumentRecord? {
        val current = repository.getById(id) ?: return null
        val updated = current.copy(displayName = name.trim().take(80), type = type, apartmentId = apartmentId)
        repository.save(updated)
        return updated
    }
}

class DeleteDocumentUseCase(
    private val storage: SecureFileStorage,
    private val repository: DocumentRepository
) {
    suspend operator fun invoke(id: DocumentId) {
        val doc = repository.getById(id) ?: return
        repository.delete(id)                                  // zuerst die DB: lieber eine verwaiste Datei
        runCatching { storage.delete(doc.filePathLocal) }      // als ein Eintrag ohne Datei
    }
}

class ShareDocumentUseCase(
    private val storage: SecureFileStorage,
    private val repository: DocumentRepository,
    private val sharer: FileSharer
) {
    suspend operator fun invoke(id: DocumentId) {
        val doc = repository.getById(id) ?: error("Dokument nicht gefunden.")
        val bytes = storage.read(doc.filePathLocal)
        val mime = detectMimeType(bytes) ?: "application/octet-stream"
        sharer.shareFile(bytes, "${safeFileName(doc.displayName)}.${extensionFor(mime)}", mime)
    }

    private fun safeFileName(name: String): String =
        name.map { if (it.isLetterOrDigit() || it == ' ' || it == '-' || it == '_') it else '_' }
            .joinToString("").trim().ifEmpty { "Dokument" }.take(60)
}