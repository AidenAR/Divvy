package com.example.divvy.ui.joingroup.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.AuthRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.backend.MemberRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JoinGroupUiState(
    val groupName: String = "",
    val isLoading: Boolean = false,
    val joined: Boolean = false,
    val alreadyMember: Boolean = false,
    val error: String? = null
)

@HiltViewModel(assistedFactory = JoinGroupViewModel.Factory::class)
class JoinGroupViewModel @AssistedInject constructor(
    @Assisted val groupId: String,
    @Assisted val groupName: String,
    private val memberRepository: MemberRepository,
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(groupId: String, groupName: String): JoinGroupViewModel
    }

    private val _uiState = MutableStateFlow(JoinGroupUiState(groupName = groupName))
    val uiState: StateFlow<JoinGroupUiState> = _uiState.asStateFlow()

    init {
        checkMembership()
    }

    private fun checkMembership() {
        viewModelScope.launch {
            val myId = authRepository.getCurrentUserId()
            memberRepository.refreshMembers(groupId)
            memberRepository.getMembers(groupId).collect { members ->
                if (members.any { it.userId == myId }) {
                    _uiState.update { it.copy(alreadyMember = true) }
                }
            }
        }
    }

    fun onJoin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                memberRepository.addMember(groupId, authRepository.getCurrentUserId())
                groupRepository.refreshGroups()
                _uiState.update { it.copy(isLoading = false, joined = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to join group. Please try again.") }
            }
        }
    }
}
