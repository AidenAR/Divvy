package com.example.divvy.ui.auth.Views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.divvy.ui.auth.ViewModels.AuthFlowViewModel
import com.example.divvy.ui.auth.ViewModels.OAuthFlow
import io.github.jan.supabase.gotrue.SessionStatus

@Composable
fun AuthNav(
    onAuthenticated: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: AuthFlowViewModel = viewModel()
    val sessionStatus by viewModel.sessionStatus.collectAsState()

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.Authenticated) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == "name") return@LaunchedEffect
            val hasProfile = viewModel.hasProfile()
            if (!hasProfile) {
                navController.navigate("name") {
                    popUpTo("launch") { inclusive = false }
                    launchSingleTop = true
                }
                return@LaunchedEffect
            }
            when (viewModel.consumeOAuthFlow()) {
                OAuthFlow.CREATE -> onAuthenticated()
                OAuthFlow.LOGIN -> onAuthenticated()
                null -> {
                    if (currentRoute == "launch" ||
                        currentRoute == "login_start" ||
                        currentRoute == "login_phone" ||
                        currentRoute == "verify_phone_login" ||
                        currentRoute == "create_phone" ||
                        currentRoute == "verify_phone_create"
                    ) {
                        onAuthenticated()
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "launch",
        modifier = Modifier.fillMaxSize()
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
                onPhone = { navController.navigate("create_phone") },
                onGoogle = { viewModel.startGoogleSignIn(flow = OAuthFlow.CREATE) }
            )
        }
        composable("create_phone") {
            CreatePhoneScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("verify_phone_create") }
            )
        }
        composable("verify_phone_create") {
            VerifyPhoneScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("name") },
                onChangePhone = { navController.popBackStack("create_phone", inclusive = false) }
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
                onPhone = { navController.navigate("login_phone") },
                onGoogle = { viewModel.startGoogleSignIn(flow = OAuthFlow.LOGIN) }
            )
        }
        composable("login_phone") {
            LoginPhoneScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("verify_phone_login") }
            )
        }
        composable("verify_phone_login") {
            VerifyPhoneLoginScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onLogin = onAuthenticated,
                onChangePhone = { navController.popBackStack("login_phone", inclusive = false) }
            )
        }
    }
}
