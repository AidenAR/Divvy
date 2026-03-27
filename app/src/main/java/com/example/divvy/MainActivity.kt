package com.example.divvy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.divvy.notifications.ExpenseNotificationService
import com.example.divvy.offline.NetworkMonitor
import com.example.divvy.offline.OfflineSyncManager
import com.example.divvy.ui.MainScreen
import com.example.divvy.ui.theme.DivvyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var expenseNotificationService: ExpenseNotificationService
    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var syncManager: OfflineSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start the Realtime subscription now that the user is authenticated.
        // stop() is called from ProfileViewModel.signOut() to close the socket cleanly.
        expenseNotificationService.start()
        setContent {
            DivvyTheme {
                MainScreen(networkMonitor, syncManager)
            }
        }
    }
}
