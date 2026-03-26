package com.example.divvy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import io.sentry.SentryLevel
import timber.log.Timber

@HiltAndroidApp
class DivvyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Debug builds get the standard logcat tree; both builds get Sentry breadcrumbs
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(SentryTree())

        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            // Tag every event with the build type so you can filter prod vs debug in Sentry
            options.environment = BuildConfig.BUILD_TYPE          // "release" | "debug"
            options.release    = "divvy@${BuildConfig.VERSION_NAME}"
            // Capture 100 % of sessions in production; tune down once volume is high
            options.tracesSampleRate       = 1.0
            options.profilesSampleRate     = 1.0
            // Don't send PII — user IDs are hashed UUIDs, never names/emails/amounts
            options.isSendDefaultPii = false
            // Breadcrumbs help trace the path to a crash; 50 is enough without bloat
            options.maxBreadcrumbs = 50
            // Only log Sentry's own internals at WARNING level to keep Logcat clean
            options.setDiagnosticLevel(SentryLevel.WARNING)
        }
    }
}
