package com.example.divvy.backend

import com.example.divvy.BuildConfig
import com.example.divvy.ui.auth.DummyAccount
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    fun getCurrentUserId(): String
}

@Singleton
class SupabaseAuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override fun getCurrentUserId(): String =
        if (BuildConfig.AUTH_BYPASS) DummyAccount.USER_ID
        else supabaseClient.auth.currentUserOrNull()?.id
            ?: error("No authenticated user")
}
