package com.example.divvy.ui.auth.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.divvy.components.AuthTextField
import com.example.divvy.ui.auth.ViewModels.AuthFlowViewModel

@Composable
fun LaunchScreen(
    onCreateAccount: () -> Unit,
    onLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthGradientBackground())
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Welcome to Divvy",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Stop chasing. Start settling.\n",
                fontSize = 14.sp,
                color = Color(0xFFE6E0F5),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            AuthPrimaryButton(
                label = "Create an account",
                modifier = Modifier.fillMaxWidth(),
                onClick = onCreateAccount
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    color = Color(0xFFE6E0F5),
                    fontSize = 13.sp
                )
                Text(
                    text = "Log in",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onLogin() }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CreateStartScreen(
    onBack: () -> Unit,
    onEmail: () -> Unit,
    onGoogle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Create new account", onBack = onBack)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Begin with creating new free account. This\n" +
                "helps you keep your learning way easier.",
            textAlign = TextAlign.Center,
            color = MutedText,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(28.dp))
        AuthPrimaryButton(
            label = "Continue with email",
            modifier = Modifier.fillMaxWidth(),
            onClick = onEmail
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "or", color = MutedText)
        Spacer(modifier = Modifier.height(16.dp))
        AuthOutlinedButton(
            label = "Continue with Google",
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoogle
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "By using Divvy, you agree to the",
            color = MutedText,
            fontSize = 12.sp
        )
        Text(
            text = "Terms and Privacy Policy.",
            color = Color(0xFF1F2937),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CreateEmailScreen(
    viewModel: AuthFlowViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Add your email 1/3", onBack = onBack)
        StepIndicator(step = 1)
        Spacer(modifier = Modifier.height(20.dp))
        AuthTextField(
            value = state.email,
            onValueChange = viewModel::updateEmail,
            placeholder = "Email",
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthPrimaryButton(
            label = if (state.isLoading) "Sending..." else "Create an account",
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { viewModel.sendEmailOtp(onSuccess = onNext) }
        )
        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = state.errorMessage ?: "", color = Color(0xFFB42318), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "By using Divvy, you agree to the",
            color = MutedText,
            fontSize = 12.sp
        )
        Text(
            text = "Terms and Privacy Policy.",
            color = Color(0xFF1F2937),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun VerifyEmailScreen(
    viewModel: AuthFlowViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onChangeEmail: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Verify your email 2/3", onBack = onBack)
        StepIndicator(step = 2)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "We just sent 6-digit code to\n${state.email}, enter it below:",
            textAlign = TextAlign.Center,
            color = MutedText
        )
        Spacer(modifier = Modifier.height(16.dp))
        OtpInputRow(value = state.otp, onValueChange = viewModel::updateOtp)
        Spacer(modifier = Modifier.height(16.dp))
        AuthPrimaryButton(
            label = if (state.isLoading) "Verifying..." else "Verify email",
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { viewModel.verifyEmailOtp(onSuccess = onNext) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Wrong email? Send to different email",
            color = Color(0xFF1F2937),
            fontSize = 12.sp,
            modifier = Modifier.clickable { onChangeEmail() }
        )
        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = state.errorMessage ?: "", color = Color(0xFFB42318), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "By using Divvy, you agree to the",
            color = MutedText,
            fontSize = 12.sp
        )
        Text(
            text = "Terms and Privacy Policy.",
            color = Color(0xFF1F2937),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CreatePasswordScreen(
    viewModel: AuthFlowViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showPassword by remember { mutableStateOf(false) }
    val hasLength = state.password.length >= 8
    val hasNumber = state.password.any { it.isDigit() }
    val hasSymbol = state.password.any { !it.isLetterOrDigit() }
    val score = listOf(hasLength, hasNumber, hasSymbol).count { it }
    val strengthColor = when (score) {
        3 -> Color(0xFF16A34A)
        2 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Create your password 3/3", onBack = onBack)
        StepIndicator(step = 3)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::updatePassword,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Enter password", color = MutedText) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { showPassword = !showPassword }
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = Color(0xFFD7D6DF),
                cursorColor = PurplePrimary,
                backgroundColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color(0xFFE8E6F2), RoundedCornerShape(10.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (score / 3f).coerceAtLeast(0.1f))
                    .height(4.dp)
                    .background(strengthColor, RoundedCornerShape(10.dp))
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PasswordRuleRow(label = "8 characters minimum", satisfied = hasLength)
            PasswordRuleRow(label = "a number", satisfied = hasNumber)
            PasswordRuleRow(label = "one symbol minimum", satisfied = hasSymbol)
        }
        Spacer(modifier = Modifier.height(16.dp))
        AuthPrimaryButton(
            label = if (state.isLoading) "Saving..." else "Continue",
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && hasLength && hasNumber && hasSymbol,
            onClick = { viewModel.setPassword(onSuccess = onNext) }
        )
        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = state.errorMessage ?: "", color = Color(0xFFB42318), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "By using Divvy, you agree to the",
            color = MutedText,
            fontSize = 12.sp
        )
        Text(
            text = "Terms and Privacy Policy.",
            color = Color(0xFF1F2937),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun NameScreen(
    viewModel: AuthFlowViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Tell us about you", onBack = onBack)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Add your name so friends can find you.",
            color = MutedText
        )
        Spacer(modifier = Modifier.height(20.dp))
        AuthTextField(
            value = state.firstName,
            onValueChange = viewModel::updateFirstName,
            placeholder = "First name",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthTextField(
            value = state.lastName,
            onValueChange = viewModel::updateLastName,
            placeholder = "Last name",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(18.dp))
        AuthPrimaryButton(
            label = if (state.isLoading) "Saving..." else "Continue",
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { viewModel.saveProfile(onSuccess = onDone) }
        )
        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = state.errorMessage ?: "", color = Color(0xFFB42318), fontSize = 12.sp)
        }
    }
}

@Composable
fun LoginStartScreen(
    onBack: () -> Unit,
    onEmail: () -> Unit,
    onGoogle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Log into account", onBack = onBack)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Welcome back!", color = MutedText)
        Text(text = "Let's continue learning", color = MutedText)
        Spacer(modifier = Modifier.height(24.dp))
        AuthPrimaryButton(
            label = "Continue with email",
            modifier = Modifier.fillMaxWidth(),
            onClick = onEmail
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "or", color = MutedText)
        Spacer(modifier = Modifier.height(14.dp))
        AuthOutlinedButton(
            label = "Continue with Apple",
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(10.dp))
        AuthOutlinedButton(
            label = "Continue with Facebook",
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(10.dp))
        AuthOutlinedButton(
            label = "Continue with Google",
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoogle
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "By using Divvy, you agree to the",
            color = MutedText,
            fontSize = 12.sp
        )
        Text(
            text = "Terms and Privacy Policy.",
            color = Color(0xFF1F2937),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun LoginEmailScreen(
    viewModel: AuthFlowViewModel,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showPassword by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Log into account", onBack = onBack)
        Spacer(modifier = Modifier.height(10.dp))
        AuthTextField(
            value = state.loginEmail,
            onValueChange = viewModel::updateLoginEmail,
            placeholder = "Email",
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.loginPassword,
            onValueChange = viewModel::updateLoginPassword,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Enter password", color = MutedText) },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Icon(
                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { showPassword = !showPassword }
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = Color(0xFFD7D6DF),
                cursorColor = PurplePrimary,
                backgroundColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(14.dp))
        AuthPrimaryButton(
            label = if (state.isLoading) "Logging in..." else "Log in",
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { viewModel.loginWithEmail(onSuccess = onLogin) }
        )
        TextButton(onClick = { viewModel.sendPasswordReset(onSuccess = onForgotPassword) }) {
            Text(text = "Forgot password?", fontSize = 12.sp)
        }
        if (state.errorMessage != null) {
            Text(text = state.errorMessage ?: "", color = Color(0xFFB42318), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "By using Divvy, you agree to the",
            color = MutedText,
            fontSize = 12.sp
        )
        Text(
            text = "Terms and Privacy Policy.",
            color = Color(0xFF1F2937),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
