package com.example.mietcheck.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MietcheckApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MietcheckApplication)
            modules(
                dataModule,         // KMP Common Core
                useCaseModule, // KMP Android-Spezifisches (DB, Billing, DataStore)
                presentationModule             // Android ViewModels & App-UI-Services
            )
        }
    }
}