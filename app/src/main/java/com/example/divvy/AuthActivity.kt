package com.example.divvy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.divvy.backend.SupabaseClientProvider
import com.example.divvy.ui.auth.Views.AuthNav
import com.example.divvy.ui.theme.DivvyTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.gotrue.handleDeeplinks

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FeatureFlags.AUTH_BYPASS) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        enableEdgeToEdge()
        if (SupabaseClientProvider.isConfigured()) {
            SupabaseClientProvider.client.handleDeeplinks(intent)
        }
        setContent {
            DivvyTheme {
                AuthNav(
                    onAuthenticated = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (SupabaseClientProvider.isConfigured()) {
            SupabaseClientProvider.client.handleDeeplinks(intent)
        }
    }
}
