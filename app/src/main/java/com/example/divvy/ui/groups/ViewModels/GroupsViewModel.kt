package com.example.divvy.ui.groups.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.divvy.backend.GroupsRepository
import com.example.divvy.backend.StubGroupsRepository
import com.example.divvy.models.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageGroupsUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false
)

class GroupsViewModel(
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { GroupsViewModel(StubGroupsRepository()) }
        }
    }

    private val _uiState = MutableStateFlow(ManageGroupsUiState(isLoading = true))
    val uiState: StateFlow<ManageGroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val groups = groupsRepository.listGroups()
            _uiState.update { ManageGroupsUiState(groups = groups, isLoading = false) }
        }
    }
}