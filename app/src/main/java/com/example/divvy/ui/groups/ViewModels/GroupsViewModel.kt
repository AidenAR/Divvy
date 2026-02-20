package com.example.divvy.ui.groups.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.GroupsRepository
import com.example.divvy.models.Group
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageGroupsUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageGroupsUiState(isLoading = true))
    val uiState: StateFlow<ManageGroupsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val groups = groupsRepository.listGroups()
            _uiState.update { ManageGroupsUiState(groups = groups, isLoading = false) }
        }
    }
}