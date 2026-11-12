// androidMain: di/PlatformModule.android.kt (ersetzt die bisherige Datei)
package com.example.shared.di

import android.content.Context
import android.content.Intent
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.shared.data.local.DatabaseKeyProvider
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.domain.platform.AndroidExportArchiveWriter
import com.example.shared.domain.platform.AndroidFileSharer
import com.example.shared.domain.platform.AndroidMediaVault
import com.example.shared.domain.platform.AppDataEraser
import com.example.shared.domain.platform.BiometricAuthenticator
import com.example.shared.domain.platform.CurrentActivityProvider
import com.example.shared.domain.platform.ExportArchiveWriter
import com.example.shared.domain.platform.FileSharer
import com.example.shared.domain.platform.PinHasher
import com.example.shared.domain.platform.SecureFileStorage
import com.example.shared.domain.platform.TextSharer
import com.example.shared.domain.repository.MediaVault
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<SqlDriver> { createEncryptedDriver(androidContext()) }
    single<TextSharer> { AndroidTextSharer(androidContext()) }
    single(createdAtStart = true) { CurrentActivityProvider(androidApplication()) }
    single { BiometricAuthenticator(get()) }
    single { SecureFileStorage(androidContext()) }
    single { PinHasher() }
    single { AppDataEraser(androidContext()) }
    single<FileSharer> { AndroidFileSharer(androidContext(),get()) }
    single { AndroidMediaVault(androidContext(), get()) }
    single<MediaVault> { get<AndroidMediaVault>() }
    factory<ExportArchiveWriter> { AndroidExportArchiveWriter(androidContext(), get(),get()) }
}

private fun createEncryptedDriver(context: Context): SqlDriver {
    System.loadLibrary("sqlcipher")
    val passphrase = DatabaseKeyProvider(context).getOrCreatePassphrase()
    return AndroidSqliteDriver(
        schema = MietCheckDatabase.Schema,
        context = context,
        name = "mietcheck.db",
        factory = SupportOpenHelperFactory(passphrase)   // überschreibt das Passwort nach Gebrauch im Speicher
    )
}

private class AndroidTextSharer(private val context: Context) : TextSharer {
    override fun shareText(subject: String, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}