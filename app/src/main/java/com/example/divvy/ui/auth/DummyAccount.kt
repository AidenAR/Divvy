package com.example.divvy.ui.auth

import com.example.divvy.FeatureFlags
import com.example.divvy.models.ProfileRow

/**
 * Dummy user and profile used when auth bypass is enabled (AUTH_BYPASS in local.properties).
 * Provides a consistent fake account so the app can run without Supabase auth.
 */
object DummyAccount {
    const val USER_ID = "dummy-user-divvy"

    val profile: ProfileRow = ProfileRow(
        id = USER_ID,
        firstName = "Demo",
        lastName = "User",
        createdAt = null,
        authMethod = "PHONE",
        email = "demo@example.com",
        phone = "+15550000000",
        phoneVerified = true
    )
}