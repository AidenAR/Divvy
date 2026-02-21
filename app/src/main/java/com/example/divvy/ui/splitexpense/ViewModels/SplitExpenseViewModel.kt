package com.example.divvy.ui.splitexpense.ViewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.GroupsRepository
import com.example.divvy.models.Group
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplitMethod(val title: String, val subtitle: String) {
    Equally("Split Equally", "Everyone pays the same amount"),
    ByPercentage("By Percentage", "Custom percentage for each person"),
    ByItems("By Items", "Assign individual items to people")
}

data class SplitExpenseUiState(
    val amount: String = "",
    val description: String = "",
    val groups: List<Group> = emptyList(),
    val selectedGroupId: String? = null,
    val splitMethod: SplitMethod = SplitMethod.Equally,
    val isLoading: Boolean = false,
    val isCreating: Boolean = false
)

@HiltViewModel
class SplitExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupsRepository: GroupsRepository,
    private val expensesRepository: ExpensesRepository
) : ViewModel() {

    private val scannedAmount: String = savedStateHandle["scannedAmount"] ?: ""
    private val scannedDescription: String = savedStateHandle["scannedDescription"] ?: ""

    private val _uiState = MutableStateFlow(
        SplitExpenseUiState(
            isLoading = true,
            amount = scannedAmount,
            description = scannedDescription
        )
    )
    val uiState: StateFlow<SplitExpenseUiState> = _uiState.asStateFlow()

    sealed interface SplitEvent {
        data object Created : SplitEvent
        data class GoToAssignItems(
            val groupId: String,
            val amount: String,
            val description: String
        ) : SplitEvent
        data class GoToSplitByPercentage(
            val groupId: String,
            val amount: String,
            val description: String
        ) : SplitEvent
    }

    private val _events = Channel<SplitEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { loadGroups() }

    private fun loadGroups() {
        viewModelScope.launch {
            val groups = groupsRepository.listGroups()
            _uiState.update {
                it.copy(
                    groups = groups,
                    selectedGroupId = groups.firstOrNull()?.id,
                    isLoading = false
                )
            }
        }
    }

    fun onAmountChange(value: String) {
        val filtered = value.filter { c -> c.isDigit() || c == '.' }
        val dotCount = filtered.count { it == '.' }
        if (dotCount > 1) return
        val dotIndex = filtered.indexOf('.')
        if (dotIndex != -1 && filtered.length - dotIndex - 1 > 2) return
        _uiState.update { it.copy(amount = filtered) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onGroupSelected(groupId: String) {
        _uiState.update { it.copy(selectedGroupId = groupId) }
    }

    fun onSplitMethodSelected(method: SplitMethod) {
        _uiState.update { it.copy(splitMethod = method) }
    }

    fun onCreateSplit() {
        val state = _uiState.value
        val groupId = state.selectedGroupId ?: return
        if (state.amount.toDoubleOrNull() == null) return

        val desc = state.description.trim().ifBlank { "Expense" }

        if (state.splitMethod == SplitMethod.ByItems) {
            viewModelScope.launch {
                _events.send(SplitEvent.GoToAssignItems(groupId, state.amount, desc))
            }
            return
        }

        if (state.splitMethod == SplitMethod.ByPercentage) {
            viewModelScope.launch {
                _events.send(SplitEvent.GoToSplitByPercentage(groupId, state.amount, desc))
            }
            return
        }

        val amountCents = (state.amount.toDouble() * 100).toLong()
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            expensesRepository.createExpense(
                groupId = groupId,
                description = state.description.trim().ifBlank { "Expense" },
                amountCents = amountCents,
                splitMethod = state.splitMethod.name
            )
            _uiState.update { it.copy(isCreating = false) }
            _events.send(SplitEvent.Created)
        }
    }
}
