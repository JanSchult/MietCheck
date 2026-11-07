package com.example.mietcheck.di

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.shared.di.appModules
import com.example.shared.domain.lock.AppLockManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MietCheckApp : Application() {

    private val lockManager: AppLockManager by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MietCheckApp)
            modules(appModules)
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = lockManager.onAppForegrounded()
            override fun onStop(owner: LifecycleOwner) = lockManager.onAppBackgrounded()
        })
    }
}