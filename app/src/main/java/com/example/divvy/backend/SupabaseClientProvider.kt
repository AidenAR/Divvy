package com.example.divvy.backend

import com.example.divvy.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.ExternalAuthAction
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {
    private var clientInstance: SupabaseClient? = null

    val client: SupabaseClient
        get() {
            if (clientInstance == null) {
                clientInstance = createSupabaseClient(
                    supabaseUrl = BuildConfig.SUPABASE_URL,
                    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
                ) {
                    install(Auth) {
                        scheme = "com.example.divvy"
                        host = "auth"
                        defaultExternalAuthAction = ExternalAuthAction.CUSTOM_TABS
                    }
                    install(Postgrest)
                }
            }
            return clientInstance!!
        }

    fun isInitialized(): Boolean = clientInstance != null

    fun isConfigured(): Boolean {
        return BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
    }
}
