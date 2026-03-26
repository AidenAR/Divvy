package com.example.divvy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import com.example.divvy.notifications.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sentry.android.core.SentryAndroid
import io.sentry.SentryLevel
import timber.log.Timber

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun notificationHelper(): NotificationHelper
}

@HiltAndroidApp
class DivvyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(SentryTree())

        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.environment = BuildConfig.BUILD_TYPE
            options.release    = "divvy@${BuildConfig.VERSION_NAME}"
            options.tracesSampleRate       = 1.0
            options.profilesSampleRate     = 1.0
            options.isSendDefaultPii = false
            options.maxBreadcrumbs = 50
            options.setDiagnosticLevel(SentryLevel.WARNING)
        }

        // Create notification channels once at startup (safe to call multiple times).
        EntryPointAccessors
            .fromApplication(this, AppEntryPoint::class.java)
            .notificationHelper()
            .createChannels()
    }
}