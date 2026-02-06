package com.example.divvy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.divvy.ui.auth.Views.AuthScreen
import com.example.divvy.ui.theme.DivvyTheme

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DivvyTheme {
                AuthScreen()
            }
        }
    }
}
