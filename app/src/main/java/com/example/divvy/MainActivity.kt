package com.example.divvy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.divvy.ui.MainScreen
import com.example.divvy.ui.theme.DivvyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DivvyTheme {
                MainScreen()
            }
        }
    }
}
