package com.example.divvy.ui.auth.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.SupabaseClientProvider
import com.example.divvy.backend.SupabaseProfilesRepository
import com.example.divvy.models.ProfileRow
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OAuthFlow {
    CREATE,
    LOGIN
}

data class AuthFlowState(
    val email: String = "",
    val otp: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val loginEmail: String = "",
    val loginPassword: String = "",
    val authMethod: String? = null,
    val otpSent: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthFlowViewModel : ViewModel() {
    private val profilesRepository = SupabaseProfilesRepository()

    private val _state = MutableStateFlow(AuthFlowState())
    val state: StateFlow<AuthFlowState> = _state.asStateFlow()

    private var pendingOAuthFlow: OAuthFlow? = null

    val sessionStatus: StateFlow<SessionStatus> = if (SupabaseClientProvider.isConfigured()) {
        SupabaseClientProvider.client.auth.sessionStatus
    } else {
        MutableStateFlow(SessionStatus.NotAuthenticated)
    }

    fun updateEmail(value: String) = _state.update {
        it.copy(email = value, otp = "", otpSent = false, errorMessage = null, authMethod = "MANUAL")
    }
    fun updateOtp(value: String) = _state.update { it.copy(otp = value, errorMessage = null) }
    fun updatePassword(value: String) = _state.update { it.copy(password = value, errorMessage = null) }
    fun updateFirstName(value: String) = _state.update { it.copy(firstName = value, errorMessage = null) }
    fun updateLastName(value: String) = _state.update { it.copy(lastName = value, errorMessage = null) }
    fun updateLoginEmail(value: String) = _state.update { it.copy(loginEmail = value, errorMessage = null) }
    fun updateLoginPassword(value: String) = _state.update { it.copy(loginPassword = value, errorMessage = null) }

    fun startGoogleSignIn(flow: OAuthFlow) {
        if (!checkConfigured()) return
        pendingOAuthFlow = flow
        _state.update { it.copy(authMethod = "GOOGLE") }
        launchAuth {
            SupabaseClientProvider.client.auth.signInWith(Google)
        }
    }

    fun sendEmailOtp(onSuccess: () -> Unit) {
        if (!checkConfigured()) return
        val email = state.value.email.trim()
        if (email.isBlank()) {
            _state.update { it.copy(errorMessage = "Email is required.") }
            return
        }
        _state.update { it.copy(authMethod = "MANUAL") }
        launchAuth {
            SupabaseClientProvider.client.auth.signInWith(OTP) {
                this.email = email
                createUser = true
            }
            _state.update { it.copy(otpSent = true) }
            onSuccess()
        }
    }

    fun verifyEmailOtp(onSuccess: () -> Unit) {
        if (!checkConfigured()) return
        val email = state.value.email.trim()
        val token = state.value.otp.trim()
        if (email.isBlank() || token.length != 6) {
            _state.update { it.copy(errorMessage = "Enter the 6-digit code.") }
            return
        }
        launchAuth {
            SupabaseClientProvider.client.auth.verifyEmailOtp(
                type = OtpType.Email.SIGNUP,
                email = email,
                token = token
            )
            onSuccess()
        }
    }

    fun setPassword(onSuccess: () -> Unit) {
        if (!checkConfigured()) return
        val password = state.value.password
        if (password.isBlank()) {
            _state.update { it.copy(errorMessage = "Password is required.") }
            return
        }
        launchAuth {
            SupabaseClientProvider.client.auth.modifyUser { this.password = password }
            onSuccess()
        }
    }

    fun loginWithEmail(onSuccess: () -> Unit) {
        if (!checkConfigured()) return
        val email = state.value.loginEmail.trim()
        val password = state.value.loginPassword
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(errorMessage = "Email and password are required.") }
            return
        }
        _state.update { it.copy(authMethod = "MANUAL") }
        launchAuth {
            SupabaseClientProvider.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            onSuccess()
        }
    }

    fun sendPasswordReset(onSuccess: (String) -> Unit) {
        if (!checkConfigured()) return
        val email = state.value.loginEmail.trim()
        if (email.isBlank()) {
            _state.update { it.copy(errorMessage = "Enter your email first.") }
            return
        }
        launchAuth {
            SupabaseClientProvider.client.auth.resetPasswordForEmail(email)
            onSuccess("Password reset email sent.")
        }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        if (!checkConfigured()) return
        val first = state.value.firstName.trim()
        val last = state.value.lastName.trim()
        if (first.isBlank() || last.isBlank()) {
            _state.update { it.copy(errorMessage = "First and last name are required.") }
            return
        }
        launchAuth {
            val user = SupabaseClientProvider.client.auth.currentUserOrNull()
                ?: SupabaseClientProvider.client.auth.retrieveUserForCurrentSession(updateSession = true)
            val email = user.email
            val method = state.value.authMethod ?: "MANUAL"
            profilesRepository.upsertProfile(
                ProfileRow(
                    id = user.id,
                    firstName = first,
                    lastName = last,
                    authMethod = method,
                    email = email
                )
            )
            onSuccess()
        }
    }

    fun consumeOAuthFlow(): OAuthFlow? {
        val flow = pendingOAuthFlow
        pendingOAuthFlow = null
        return flow
    }

    private fun checkConfigured(): Boolean {
        if (!SupabaseClientProvider.isConfigured()) {
            _state.update {
                it.copy(
                    errorMessage = "Missing Supabase keys. Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties."
                )
            }
            return false
        }
        return true
    }

    private fun launchAuth(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                block()
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message ?: "Something went wrong.") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
