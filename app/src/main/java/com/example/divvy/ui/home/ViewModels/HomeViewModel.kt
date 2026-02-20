package com.example.divvy.ui.home.ViewModels

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

data class HomeUiState(
    val groups: List<Group> = emptyList(),
    val totalOwedCents: Long = 0L,
    val totalOwingCents: Long = 0L,
    val isLoading: Boolean = false
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

    init {
        viewModelScope.launch {
            val groups = groupsRepository.listGroups()
            _uiState.update {
                HomeUiState(
                    groups = groups,
                    totalOwedCents = groups.filter { it.balanceCents > 0 }.sumOf { it.balanceCents },
                    totalOwingCents = groups.filter { it.balanceCents < 0 }.sumOf { kotlin.math.abs(it.balanceCents) },
                    isLoading = false
                )
            }
        }
    }
}
