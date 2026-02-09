package com.example.divvy.ui.home.ViewModels

import androidx.lifecycle.ViewModel
import com.example.divvy.models.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadFakeData()
    }

    private fun loadFakeData() {
        val fakeGroups = listOf(
            Group(
                id = "1",
                name = "Roommates",
                iconName = "Home",
                memberCount = 3,
                balanceCents = 16850L
            ),
            Group(
                id = "2",
                name = "Weekend Trip",
                iconName = "Flight",
                memberCount = 5,
                balanceCents = -8730L
            ),
            Group(
                id = "3",
                name = "Work Lunch",
                iconName = "Restaurant",
                memberCount = 4,
                balanceCents = 2240L
            )
        )

        _uiState.value = HomeUiState(
            groups = fakeGroups,
            totalOwedCents = fakeGroups.filter { it.balanceCents > 0 }.sumOf { it.balanceCents },
            totalOwingCents = fakeGroups.filter { it.balanceCents < 0 }.sumOf { kotlin.math.abs(it.balanceCents) },
            isLoading = false
        )
    }
}
