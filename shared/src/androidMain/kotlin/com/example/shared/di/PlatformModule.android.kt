package com.example.shared.di

import android.content.Context
import android.content.Intent
import com.example.shared.domain.platform.TextSharer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule = module {
    single<TextSharer> { AndroidTextSharer(androidContext()) }
}

private class AndroidTextSharer(private val context: Context) : TextSharer {
    override fun shareText(subject: String, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}