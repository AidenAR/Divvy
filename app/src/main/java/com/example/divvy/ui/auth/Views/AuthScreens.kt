package com.example.divvy.ui.auth.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthGradientBackground())
            .padding(24.dp),
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

@Composable
fun CreateStartScreen(
    onBack: () -> Unit,
    onPhone: () -> Unit,
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
            text = "Begin with creating new free account.",
            textAlign = TextAlign.Center,
            color = MutedText,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(28.dp))
        AuthPrimaryButton(
            label = "Continue with phone",
            modifier = Modifier.fillMaxWidth(),
            onClick = onPhone
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
fun CreatePhoneScreen(
    viewModel: AuthFlowViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Add your phone 1/3", onBack = onBack)
        StepIndicator(step = 1)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "We'll text you a 6-digit code to verify.",
            textAlign = TextAlign.Center,
            color = MutedText
        )
        Spacer(modifier = Modifier.height(20.dp))
        PhoneNumberField(
            countryCode = state.countryCode,
            countryFlag = state.countryFlag,
            phoneDigits = state.phoneDigits,
            onCountryChange = viewModel::updateCountryOption,
            onPhoneDigitsChange = viewModel::updatePhoneDigits,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthPrimaryButton(
            label = if (state.isLoading) "Sending..." else "Send code",
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { viewModel.sendPhoneOtp(createUser = true, onSuccess = onNext) }
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
fun VerifyPhoneScreen(
    viewModel: AuthFlowViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onChangePhone: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Verify your phone 2/3", onBack = onBack)
        StepIndicator(step = 2)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "We just sent 6-digit code to\n${formatPhoneNumber(state.countryCode, state.phoneDigits)}, enter it below:",
            textAlign = TextAlign.Center,
            color = MutedText
        )
        Spacer(modifier = Modifier.height(16.dp))
        OtpInputRow(value = state.otp, onValueChange = viewModel::updateOtp)
        Spacer(modifier = Modifier.height(16.dp))
        AuthPrimaryButton(
            label = if (state.isLoading) "Verifying..." else "Verify phone",
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { viewModel.verifyPhoneOtp(onSuccess = onNext) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Wrong number? Send to different phone",
            color = Color(0xFF1F2937),
            fontSize = 12.sp,
            modifier = Modifier.clickable { onChangePhone() }
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

    LaunchedEffect(Unit) {
        viewModel.prefillProfileEmail()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Tell us about you", onBack = onBack)
        StepIndicator(step = 3)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Add your phone, email, and name so friends can find you.",
            color = MutedText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        PhoneNumberField(
            countryCode = state.countryCode,
            countryFlag = state.countryFlag,
            phoneDigits = state.phoneDigits,
            onCountryChange = viewModel::updateCountryOption,
            onPhoneDigitsChange = viewModel::updatePhoneDigits,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        AuthTextField(
            value = state.profileEmail,
            onValueChange = viewModel::updateProfileEmail,
            placeholder = "Email",
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(12.dp))
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
    onPhone: () -> Unit,
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
        Text(text = "It's time to divvy up", color = MutedText)
        Spacer(modifier = Modifier.height(24.dp))
        AuthPrimaryButton(
            label = "Continue with phone",
            modifier = Modifier.fillMaxWidth(),
            onClick = onPhone
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "or", color = MutedText)
        Spacer(modifier = Modifier.height(14.dp))
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
fun LoginPhoneScreen(
    viewModel: AuthFlowViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Log into account", onBack = onBack)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Enter your phone number to continue.",
            color = MutedText
        )
        Spacer(modifier = Modifier.height(20.dp))
        PhoneNumberField(
            countryCode = state.countryCode,
            countryFlag = state.countryFlag,
            phoneDigits = state.phoneDigits,
            onCountryChange = viewModel::updateCountryOption,
            onPhoneDigitsChange = viewModel::updatePhoneDigits,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthPrimaryButton(
            label = if (state.isLoading) "Sending..." else "Send code",
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { viewModel.sendPhoneOtp(createUser = false, onSuccess = onNext) }
        )
        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = state.errorMessage ?: "", color = Color(0xFFB42318), fontSize = 12.sp)
        }
    }
}

@Composable
fun VerifyPhoneLoginScreen(
    viewModel: AuthFlowViewModel,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onChangePhone: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthTopBar(title = "Enter verification code", onBack = onBack)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "We just sent 6-digit code to\n${formatPhoneNumber(state.countryCode, state.phoneDigits)}",
            textAlign = TextAlign.Center,
            color = MutedText
        )
        Spacer(modifier = Modifier.height(16.dp))
        OtpInputRow(value = state.otp, onValueChange = viewModel::updateOtp)
        Spacer(modifier = Modifier.height(16.dp))
        AuthPrimaryButton(
            label = if (state.isLoading) "Verifying..." else "Verify & log in",
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            onClick = { viewModel.verifyPhoneOtp(onSuccess = onLogin) }
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Wrong number? Send to different phone",
            color = Color(0xFF1F2937),
            fontSize = 12.sp,
            modifier = Modifier.clickable { onChangePhone() }
        )
        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = state.errorMessage ?: "", color = Color(0xFFB42318), fontSize = 12.sp)
        }
    }
}
