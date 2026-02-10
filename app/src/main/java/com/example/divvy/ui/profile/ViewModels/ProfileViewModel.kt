package com.example.divvy.ui.profile.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.SupabaseClientProvider
import com.example.divvy.backend.SupabaseProfilesRepository
import com.example.divvy.models.ProfileRow
import io.github.jan.supabase.gotrue.auth
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: ProfileRow? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(
) : ViewModel() {
    private val profilesRepository = SupabaseProfilesRepository()
    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val user = SupabaseClientProvider.client.auth.currentUserOrNull()
                    ?: SupabaseClientProvider.client.auth.retrieveUserForCurrentSession(updateSession = true)
                val profile = profilesRepository.getProfile(user.id)
                val metadata = user.userMetadata?.jsonObject
                val avatarUrl = metadata
                    ?.get("avatar_url")
                    ?.jsonPrimitive
                    ?.content
                    ?.takeIf { it.isNotBlank() }
                    ?: metadata
                        ?.get("picture")
                        ?.jsonPrimitive
                        ?.content
                        ?.takeIf { it.isNotBlank() }
                _uiState.update {
                    it.copy(
                        profile = profile,
                        email = user.email,
                        avatarUrl = avatarUrl,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load profile.")
                }
            }
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                SupabaseClientProvider.client.auth.signOut()
                _uiState.update { it.copy(isLoading = false, profile = null, email = null) }
                onComplete()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to sign out.")
                }
            }
        }
    }
}
