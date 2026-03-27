package com.example.divvy.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.divvy.notifications.RequestNotificationPermission
import com.example.divvy.ui.navigation.AppDestination
import com.example.divvy.ui.navigation.AppNavHost
import com.example.divvy.ui.navigation.BottomNavigationBar

@Composable
fun MainScreen() {
    RequestNotificationPermission()
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    DisposableEffect(navController) {
        val listener = Consumer<Intent> { navController.handleDeepLink(it) }
        activity.addOnNewIntentListener(listener)
        onDispose { activity.removeOnNewIntentListener(listener) }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.hasRoute<AppDestination.Home>() == true ||
            currentDestination?.hasRoute<AppDestination.Groups>() == true ||
            currentDestination?.hasRoute<AppDestination.Friends>() == true ||
            currentDestination?.hasRoute<AppDestination.Profile>() == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}