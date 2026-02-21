package com.example.divvy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppDestination {

    @Serializable
    sealed interface BottomNav : AppDestination {
        @Serializable data object Home     : BottomNav
        @Serializable data object Groups   : BottomNav
        @Serializable data object Expenses : BottomNav
        @Serializable data object Ledger   : BottomNav
        @Serializable data object Profile  : BottomNav
    }

    @Serializable
    data class GroupDetail(val groupId: String) : AppDestination

    @Serializable
    data object ScanReceipt : AppDestination

    @Serializable
    data class SplitExpense(
        val scannedAmount: String = "",
        val scannedDescription: String = ""
    ) : AppDestination

    @Serializable
    data class AssignItems(
        val groupId: String,
        val amountDisplay: String,
        val description: String
    ) : AppDestination
}

// UI metadata for the bottom bar — separate from the navigation contract
data class BottomNavItem(
    val destination: AppDestination.BottomNav,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(AppDestination.BottomNav.Home,     "Home",     Icons.Filled.Home),
    BottomNavItem(AppDestination.BottomNav.Groups,   "Groups",   Icons.Filled.Group),
    BottomNavItem(AppDestination.BottomNav.Expenses, "Expenses", Icons.Filled.Receipt),
    BottomNavItem(AppDestination.BottomNav.Ledger,   "Ledger",   Icons.Filled.ReceiptLong),
    BottomNavItem(AppDestination.BottomNav.Profile,  "Profile",  Icons.Filled.Person),
)
