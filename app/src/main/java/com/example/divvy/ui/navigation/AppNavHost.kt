package com.example.divvy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.divvy.ui.assignitems.ViewModels.AssignItemsViewModel
import com.example.divvy.ui.assignitems.Views.AssignItemsScreen
import com.example.divvy.ui.splitpercentage.ViewModels.SplitByPercentageViewModel
import com.example.divvy.ui.splitpercentage.Views.SplitByPercentageScreen
import com.example.divvy.ui.analytics.Views.AnalyticsScreen
import com.example.divvy.ui.groupdetail.Views.GroupDetailScreen
import com.example.divvy.ui.home.Views.HomeScreen
import com.example.divvy.ui.ledger.Views.LedgerScreen
import com.example.divvy.ui.profile.Views.ProfileScreen
import com.example.divvy.ui.scanreceipt.Views.ScanReceiptScreen
import com.example.divvy.ui.splitexpense.Views.SplitExpenseScreen

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Home
    ) {
        composable<AppDestination.Home> {
            HomeScreen(
                onGroupClick = { id -> navController.navigate(AppDestination.GroupDetail(id)) },
                onAddExpense = { navController.navigate(AppDestination.SplitExpense()) },
                onScanReceipt = { navController.navigate(AppDestination.ScanReceipt) },
                onProfileClick = { navController.navigate(AppDestination.Profile) },
                onLedgerClick = { navController.navigate(AppDestination.Ledger) },
                onAnalyticsClick = { navController.navigate(AppDestination.Analytics) }
            )
        }
        composable<AppDestination.Analytics> {
            AnalyticsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<AppDestination.Ledger> {
            LedgerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<AppDestination.Profile> {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<AppDestination.GroupDetail> { backStack ->
            val dest: AppDestination.GroupDetail = backStack.toRoute()
            GroupDetailScreen(
                groupId = dest.groupId,
                onBack = { navController.popBackStack() },
                onLeaveGroup = {
                    navController.popBackStack(
                        route = AppDestination.Home,
                        inclusive = false
                    )
                },
                onAddExpense = {
                    navController.navigate(AppDestination.SplitExpense(preselectedGroupId = dest.groupId))
                }
            )
        }
        composable<AppDestination.ScanReceipt> {
            ScanReceiptScreen(
                onBack = { navController.popBackStack() },
                onScanComplete = { amount, description ->
                    navController.popBackStack()
                    navController.navigate(
                        AppDestination.SplitExpense(
                            scannedAmount = amount,
                            scannedDescription = description
                        )
                    )
                }
            )
        }
        composable<AppDestination.SplitExpense> {
            SplitExpenseScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAssignItems = { groupId, amount, description ->
                    navController.navigate(
                        AppDestination.AssignItems(groupId, amount, description)
                    )
                },
                onNavigateToSplitByPercentage = { groupId, amount, description ->
                    navController.navigate(
                        AppDestination.SplitByPercentage(groupId, amount, description)
                    )
                }
            )
        }
        composable<AppDestination.SplitByPercentage> { backStack ->
            val dest: AppDestination.SplitByPercentage = backStack.toRoute()
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<
                SplitByPercentageViewModel, SplitByPercentageViewModel.Factory
            >(
                creationCallback = { factory ->
                    factory.create(dest.groupId, dest.amountDisplay, dest.description)
                }
            )
            SplitByPercentageScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.popBackStack(
                        route = AppDestination.Home,
                        inclusive = false
                    )
                }
            )
        }
        composable<AppDestination.AssignItems> { backStack ->
            val dest: AppDestination.AssignItems = backStack.toRoute()
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<
                AssignItemsViewModel, AssignItemsViewModel.Factory
            >(
                creationCallback = { factory ->
                    factory.create(dest.groupId, dest.amountDisplay, dest.description)
                }
            )
            AssignItemsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.popBackStack(
                        route = AppDestination.Home,
                        inclusive = false
                    )
                }
            )
        }
    }
}
