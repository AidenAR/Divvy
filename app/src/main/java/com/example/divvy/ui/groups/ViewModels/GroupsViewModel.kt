package com.example.divvy.ui.groups.ViewModels

import androidx.lifecycle.ViewModel
import com.example.divvy.models.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ManageGroupsUiState(
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = false
)

class GroupsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ManageGroupsUiState())
    val uiState: StateFlow<ManageGroupsUiState> = _uiState.asStateFlow()

    init {
        loadFakeData()
    }

    private fun loadFakeData() {
        _uiState.value = ManageGroupsUiState(
            groups = listOf(
                Group(
                    id = "1",
                    name = "Roommates",
                    emoji = "\uD83C\uDFE0",
                    memberCount = 3,
                    balanceCents = 16850L
                ),
                Group(
                    id = "2",
                    name = "Weekend Trip",
                    emoji = "✈\uFE0F",
                    memberCount = 5,
                    balanceCents = -8730L
                ),
                Group(
                    id = "3",
                    name = "Work Lunch",
                    emoji = "\uD83C\uDF55",
                    memberCount = 4,
                    balanceCents = 2240L
                )
            ),
            isLoading = false
        )
    }
}
