package com.example.divvy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface AppDestination {
    val route: String

    sealed interface BottomNav : AppDestination {
        val label: String
        val icon: ImageVector

        data object Home : BottomNav {
            override val route = "home"
            override val label = "Home"
            override val icon = Icons.Filled.Home
        }

        data object Groups : BottomNav {
            override val route = "groups"
            override val label = "Groups"
            override val icon = Icons.Filled.Group
        }

        data object Expenses : BottomNav {
            override val route = "expenses"
            override val label = "Expenses"
            override val icon = Icons.Filled.Receipt
        }

        data object Ledger : BottomNav {
            override val route = "ledger"
            override val label = "Ledger"
            override val icon = Icons.Filled.ReceiptLong
        }

        data object Profile : BottomNav {
            override val route = "profile"
            override val label = "Profile"
            override val icon = Icons.Filled.Person
        }

        companion object {
            val all: List<BottomNav> = listOf(Home, Groups, Expenses, Ledger, Profile)
        }
    }
}
