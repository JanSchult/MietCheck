package com.example.shared.domain.platform


import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

/** Merkt sich die aktuelle FragmentActivity (nur schwach referenziert, kein Memory-Leak). */
class CurrentActivityProvider(application: Application) {

    private var ref: WeakReference<FragmentActivity>? = null

    val current: FragmentActivity? get() = ref?.get()

    init {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = remember(activity)
            override fun onActivityResumed(activity: Activity) = remember(activity)
            override fun onActivityDestroyed(activity: Activity) {
                if (ref?.get() === activity) ref = null
            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }

    private fun remember(activity: Activity) {
        if (activity is FragmentActivity) ref = WeakReference(activity)
    }
}