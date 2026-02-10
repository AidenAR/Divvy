package com.example.divvy.ui.auth.Views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.divvy.ui.auth.ViewModels.AuthFlowViewModel
import com.example.divvy.ui.auth.ViewModels.OAuthFlow
import io.github.jan.supabase.gotrue.SessionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthNav(
    onAuthenticated: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: AuthFlowViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sessionStatus by viewModel.sessionStatus.collectAsState()

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.Authenticated) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            when (viewModel.consumeOAuthFlow()) {
                OAuthFlow.CREATE -> {
                    if (currentRoute != "name") {
                        navController.navigate("name") {
                            popUpTo("launch") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
                OAuthFlow.LOGIN -> onAuthenticated()
                null -> {
                    if (currentRoute == "launch" || currentRoute == "login_start" || currentRoute == "login_email") {
                        onAuthenticated()
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "launch",
            modifier = Modifier
        ) {
            composable("launch") {
                LaunchScreen(
                    onCreateAccount = { navController.navigate("create_start") },
                    onLogin = { navController.navigate("login_start") }
                )
            }
            composable("create_start") {
                CreateStartScreen(
                    onBack = { navController.popBackStack() },
                    onEmail = { navController.navigate("create_email") },
                    onGoogle = { viewModel.startGoogleSignIn(flow = OAuthFlow.CREATE) }
                )
            }
            composable("create_email") {
                CreateEmailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate("verify_email") }
                )
            }
            composable("verify_email") {
                VerifyEmailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate("create_password") },
                    onChangeEmail = { navController.popBackStack("create_email", inclusive = false) }
                )
            }
            composable("create_password") {
                CreatePasswordScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate("name") }
                )
            }
            composable("name") {
                NameScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onDone = onAuthenticated
                )
            }
            composable("login_start") {
                LoginStartScreen(
                    onBack = { navController.popBackStack() },
                    onEmail = { navController.navigate("login_email") },
                    onGoogle = { viewModel.startGoogleSignIn(flow = OAuthFlow.LOGIN) }
                )
            }
            composable("login_email") {
                LoginEmailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onLogin = onAuthenticated,
                    onForgotPassword = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
        }
    }
}
