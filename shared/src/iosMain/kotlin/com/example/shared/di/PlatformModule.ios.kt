package com.example.shared.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.shared.data.local.MietCheckDatabase
import com.example.shared.domain.platform.TextSharer
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual val platformModule = module {
    single<SqlDriver> { NativeSqliteDriver(MietCheckDatabase.Schema, "mietcheck.db") }

    single<TextSharer> { IosTextSharer() }
}

private class IosTextSharer : TextSharer {
    override fun shareText(subject: String, text: String) {
        val controller = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.presentViewController(controller, animated = true, completion = null)
    }
}