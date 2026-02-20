package com.example.divvy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.divvy.ui.expenses.Views.ExpensesScreen
import com.example.divvy.ui.groups.Views.GroupsScreen
import com.example.divvy.ui.home.Views.HomeScreen
import com.example.divvy.ui.ledger.Views.LedgerScreen
import com.example.divvy.ui.profile.Views.ProfileScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.BottomNav.Home.route,
        modifier = modifier
    ) {
        composable(AppDestination.BottomNav.Home.route) { HomeScreen() }
        composable(AppDestination.BottomNav.Groups.route)   { GroupsScreen() }
        composable(AppDestination.BottomNav.Expenses.route) { ExpensesScreen() }
        composable(AppDestination.BottomNav.Ledger.route)   { LedgerScreen() }
        composable(AppDestination.BottomNav.Profile.route)  { ProfileScreen() }
    }
}
