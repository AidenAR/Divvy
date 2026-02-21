package com.example.divvy.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.divvy.ui.navigation.AppNavHost

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold { innerPadding ->
        AppNavHost(navController, Modifier.padding(innerPadding))
    }
}
