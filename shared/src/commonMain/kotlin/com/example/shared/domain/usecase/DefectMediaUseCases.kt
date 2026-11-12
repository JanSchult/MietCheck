package com.example.shared.domain.usecase

import com.example.shared.domain.model.StoredMedia
import com.example.shared.domain.model.ids.DefectId
import com.example.shared.domain.repository.DefectRepository
import com.example.shared.domain.repository.MediaVault

class AddDefectMediaUseCase(
    private val repository: DefectRepository,
    private val vault: MediaVault
) {
    suspend operator fun invoke(defectId: DefectId, media: StoredMedia) {
        try {
            repository.addMedia(defectId, media.storedName)
        } catch (e: Throwable) {
            vault.delete(media.storedName)          // keine verwaiste Datei zurücklassen
            throw e
        }
    }
}

class RemoveDefectMediaUseCase(
    private val repository: DefectRepository,
    private val vault: MediaVault
) {
    suspend operator fun invoke(defectId: DefectId, storedName: String) {
        repository.removeMedia(defectId, storedName)
        vault.delete(storedName)
    }
}

class OpenDefectMediaUseCase(private val vault: MediaVault) {
    suspend operator fun invoke(storedName: String, share: Boolean) = vault.open(storedName, share)
}