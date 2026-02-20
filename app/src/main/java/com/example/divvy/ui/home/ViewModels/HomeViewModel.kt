package com.example.divvy.ui.home.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.GroupsRepository
import com.example.divvy.components.GroupIcon
import com.example.divvy.models.Group
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val groups: List<Group> = emptyList(),
    val totalOwedCents: Long = 0L,
    val totalOwingCents: Long = 0L,
    val isLoading: Boolean = false,
    val showCreateGroupSheet: Boolean = false,
    val createName: String = "",
    val createIcon: GroupIcon = GroupIcon.Group,
    val isCreating: Boolean = false
) {
    val formattedOwed: String
        get() {
            val dollars = totalOwedCents / 100.0
            return "$${String.format("%.2f", dollars)}"
        }

    val formattedOwing: String
        get() {
            val dollars = totalOwingCents / 100.0
            return "$${String.format("%.2f", dollars)}"
        }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadGroups() }

    private fun loadGroups() {
        viewModelScope.launch {
            val groups = groupsRepository.listGroups()
            _uiState.update {
                it.copy(
                    groups = groups,
                    totalOwedCents = groups.filter { g -> g.balanceCents > 0 }.sumOf { g -> g.balanceCents },
                    totalOwingCents = groups.filter { g -> g.balanceCents < 0 }.sumOf { g -> kotlin.math.abs(g.balanceCents) },
                    isLoading = false
                )
            }
        }
    }

    fun onCreateGroupClick() {
        _uiState.update { it.copy(showCreateGroupSheet = true, createName = "", createIcon = GroupIcon.Group) }
    }

    fun onCreateGroupDismiss() {
        _uiState.update { it.copy(showCreateGroupSheet = false) }
    }

    fun onCreateNameChange(value: String) { _uiState.update { it.copy(createName = value) } }
    fun onCreateIconSelected(icon: GroupIcon) { _uiState.update { it.copy(createIcon = icon) } }

    fun submitCreateGroup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            groupsRepository.createGroup(_uiState.value.createName.trim(), _uiState.value.createIcon)
            _uiState.update { it.copy(isCreating = false, showCreateGroupSheet = false) }
            loadGroups()
        }
    }
}
