package com.example.shared.domain.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSDataWritingFileProtectionComplete
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToURL

actual class SecureFileStorage {
    actual suspend fun save(bytes: ByteArray, fileName: String): String {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val documentDirectory = urls.first() as NSURL
        val fileURL = documentDirectory.URLByAppendingPathComponent(fileName)!!

        val nsData = bytes.toNSData()
        // Speichern mit iOS Dateischutz (Data Protection - verschlüsselt im Locked-State)
        nsData.writeToURL(fileURL, options = NSDataWritingFileProtectionComplete)

        return fileURL.path ?: throw IllegalStateException("Invalid file path")
    }

    actual suspend fun read(path: String): ByteArray {
        val nsData = NSData.dataWithContentsOfFile(path) ?: throw IllegalStateException("File not found")
        return nsData.toByteArray()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun delete(path: String): Boolean {
        val fileManager = NSFileManager.defaultManager
        return fileManager.removeItemAtPath(path, error = null)
    }
}