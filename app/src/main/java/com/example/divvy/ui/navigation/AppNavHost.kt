package com.example.divvy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.divvy.ui.assignitems.ViewModels.AssignItemsViewModel
import com.example.divvy.ui.assignitems.Views.AssignItemsScreen
import com.example.divvy.ui.splitpercentage.ViewModels.SplitByPercentageViewModel
import com.example.divvy.ui.splitpercentage.Views.SplitByPercentageScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.divvy.ui.frienddetail.FriendDetailScreen
import com.example.divvy.ui.friends.FriendsScreen
import com.example.divvy.ui.groups.Views.GroupsScreen
import com.example.divvy.ui.home.Views.HomeScreen
import com.example.divvy.ui.analytics.Views.AnalyticsScreen
import com.example.divvy.ui.groupdetail.Views.GroupDetailScreen
import com.example.divvy.ui.ledger.Views.LedgerScreen
import com.example.divvy.ui.profile.Views.ProfileScreen
import com.example.divvy.ui.notifications.Views.NotificationsScreen
import com.example.divvy.ui.receiptreview.Views.ReceiptReviewScreen
import com.example.divvy.ui.scanreceipt.Views.ScanReceiptScreen
import com.example.divvy.ui.splitexpense.Views.SplitExpenseScreen
import com.example.divvy.ui.statementimport.Views.StatementUploadScreen
import com.example.divvy.ui.statementimport.Views.TransactionReviewScreen
import com.example.divvy.ui.statementimport.ViewModels.TransactionReviewViewModel
import androidx.navigation.navDeepLink
import com.example.divvy.ui.joingroup.ViewModels.JoinGroupViewModel
import com.example.divvy.ui.joingroup.Views.JoinGroupScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Home,
        modifier = modifier
    ) {
        composable<AppDestination.Home> {
            HomeScreen(
                onGroupClick = { id -> navController.navigate(AppDestination.GroupDetail(id)) },
                onGroupsClick = {
                    navController.navigate(AppDestination.Groups) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onAddExpense = { navController.navigate(AppDestination.SplitExpense()) },
                onLedgerClick = { navController.navigate(AppDestination.Ledger) },
                onImportStatement = { navController.navigate(AppDestination.StatementUpload) },
                onNotificationsClick = { navController.navigate(AppDestination.Notifications) }
            )
        }
        composable<AppDestination.Groups> {
            GroupsScreen(
                onGroupClick = { id -> navController.navigate(AppDestination.GroupDetail(id)) },
                onCreatedGroupNavigate = { id ->
                    navController.navigate(AppDestination.GroupDetail(id))
                }
            )
        }
        composable<AppDestination.Friends> {
            FriendsScreen(
                onFriendClick = { friendUserId ->
                    navController.navigate(AppDestination.FriendDetail(friendUserId))
                },
                onCreatedGroupNavigate = { id ->
                    navController.navigate(AppDestination.GroupDetail(id))
                },
                onAddExpenseNavigate = { groupId ->
                    navController.navigate(AppDestination.SplitExpense(preselectedGroupId = groupId))
                }
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
        composable<AppDestination.Notifications> {
            NotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<AppDestination.FriendDetail> { backStack ->
            val dest: AppDestination.FriendDetail = backStack.toRoute()
            FriendDetailScreen(
                friendUserId = dest.friendUserId,
                onBack = { navController.popBackStack() },
                onAddExpenseNavigate = { groupId ->
                    navController.navigate(AppDestination.SplitExpense(preselectedGroupId = groupId))
                }
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
                onNavigateToReview = {
                    navController.navigate(AppDestination.ReceiptReview)
                }
            )
        }
        composable<AppDestination.ReceiptReview> {
            ReceiptReviewScreen(
                onBack = { navController.popBackStack() },
                onContinue = { amount, description ->
                    navController.navigate(
                        AppDestination.SplitExpense(
                            scannedAmount = amount,
                            scannedDescription = description
                        )
                    ) {
                        popUpTo<AppDestination.ScanReceipt> { inclusive = true }
                    }
                }
            )
        }
        composable<AppDestination.SplitExpense> {
            SplitExpenseScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAssignItems = { groupId, amount, description, paidByUserId, currency ->
                    navController.navigate(
                        AppDestination.AssignItems(groupId, amount, description, paidByUserId, currency)
                    )
                },
                onNavigateToSplitByPercentage = { groupId, amount, description, paidByUserId, currency ->
                    navController.navigate(
                        AppDestination.SplitByPercentage(groupId, amount, description, paidByUserId, currency)
                    )
                }
            )
        }
        composable<AppDestination.SplitByPercentage> { backStack ->
            val dest: AppDestination.SplitByPercentage = backStack.toRoute()
            val viewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<
                    SplitByPercentageViewModel, SplitByPercentageViewModel.Factory
                    >(
                creationCallback = { factory ->
                    factory.create(dest.groupId, dest.amountDisplay, dest.description, dest.paidByUserId, dest.currency)
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
            val viewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<
                    AssignItemsViewModel, AssignItemsViewModel.Factory
                    >(
                creationCallback = { factory ->
                    factory.create(dest.groupId, dest.amountDisplay, dest.description, dest.paidByUserId, dest.currency)
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
        composable<AppDestination.StatementUpload> {
            StatementUploadScreen(
                onBack = { navController.popBackStack() },
                onTransactionsParsed = {
                    navController.navigate(AppDestination.TransactionReview) {
                        popUpTo(AppDestination.StatementUpload) { inclusive = true }
                    }
                }
            )
        }
        composable<AppDestination.TransactionReview> {
            val viewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel<
                    TransactionReviewViewModel
                    >()
            TransactionReviewScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack(
                        route = AppDestination.Home,
                        inclusive = false
                    )
                },
                onAddAsExpense = { amountCents, description ->
                    val amountStr = String.format("%.2f", amountCents / 100.0)
                    navController.navigate(
                        AppDestination.SplitExpense(
                            scannedAmount = amountStr,
                            scannedDescription = description
                        )
                    )
                },
                onDone = {
                    navController.popBackStack(
                        route = AppDestination.Home,
                        inclusive = false
                    )
                }
            )
        }
        composable<AppDestination.JoinGroup>(
            deepLinks = listOf(navDeepLink<AppDestination.JoinGroup>(basePath = "divvy://join"))
        ) { backStack ->
            val dest: AppDestination.JoinGroup = backStack.toRoute()
            JoinGroupScreen(
                groupId = dest.groupId,
                groupName = dest.groupName,
                onBack = { navController.popBackStack() },
                onJoined = { id ->
                    navController.navigate(AppDestination.GroupDetail(id)) {
                        popUpTo(AppDestination.Home) { inclusive = false }
                    }
                }
            )
        }
    }
}