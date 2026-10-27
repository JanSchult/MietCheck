package com.example.mietcheck.di

import android.app.Application
import com.example.shared.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MietcheckApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MietcheckApplication)
            modules(
                appModules      // KMP Common Core
                 // KMP Android-Spezifisches (DB, Billing, DataStore)
                             // Android ViewModels & App-UI-Services
            )
        }
    }
}