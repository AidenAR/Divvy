package com.example.divvy.ui.splitexpense.ViewModels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.ActivityRepository
import com.example.divvy.backend.AuthRepository
import com.example.divvy.backend.BalanceRepository
import com.example.divvy.backend.DataResult
import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.backend.MemberRepository
import com.example.divvy.models.Group
import com.example.divvy.models.GroupMember
import com.example.divvy.models.splitEqually
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplitMethod(val title: String, val subtitle: String) {
    Equally("Split equally", "Everyone pays the same"),
    ByPercentage("By percentage", "Custom % for each person"),
    ByItems("By items", "Assign items to people")
}

data class SplitMember(
    val id: String,
    val name: String,
    val isCurrentUser: Boolean = false,
)

data class SplitExpenseUiState(
    val amount: String = "",
    val description: String = "",
    val groups: List<Group> = emptyList(),
    val selectedGroupId: String? = null,
    val splitMethod: SplitMethod = SplitMethod.Equally,
    val members: List<SplitMember> = emptyList(),
    val paidByUserId: String = "",
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isMembersLoading: Boolean = false,
) {
    val paidByName: String
        get() = members.firstOrNull { it.id == paidByUserId }?.name ?: "You"

    val perPersonAmount: String
        get() {
            val total = amount.toDoubleOrNull() ?: return ""
            val count = members.size
            if (count == 0) return ""
            return String.format("%.2f", total / count)
        }

    val canCreate: Boolean
        get() = amount.toDoubleOrNull() != null &&
            amount.toDouble() > 0 &&
            selectedGroupId != null &&
            members.isNotEmpty()
}

@HiltViewModel
class SplitExpenseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val expensesRepository: ExpensesRepository,
    private val balanceRepository: BalanceRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val scannedAmount: String = savedStateHandle["scannedAmount"] ?: ""
    private val scannedDescription: String = savedStateHandle["scannedDescription"] ?: ""
    private val preselectedGroupId: String = savedStateHandle["preselectedGroupId"] ?: ""

    private val currentUserId = authRepository.getCurrentUserId()

    private val _uiState = MutableStateFlow(
        SplitExpenseUiState(
            isLoading = true,
            amount = scannedAmount,
            description = scannedDescription,
            paidByUserId = currentUserId,
        )
    )
    val uiState: StateFlow<SplitExpenseUiState> = _uiState.asStateFlow()

    sealed interface SplitEvent {
        data object Created : SplitEvent
        data class GoToAssignItems(val groupId: String, val amount: String, val description: String) : SplitEvent
        data class GoToSplitByPercentage(val groupId: String, val amount: String, val description: String) : SplitEvent
    }

    private val _events = Channel<SplitEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            groupRepository.listGroups().collect { result ->
                val groups = (result as? DataResult.Success)?.data ?: return@collect
                val selectedId = _uiState.value.selectedGroupId
                    ?: preselectedGroupId.takeIf { it.isNotEmpty() }
                    ?: groups.firstOrNull()?.id
                _uiState.update { current ->
                    current.copy(
                        groups = groups,
                        selectedGroupId = selectedId,
                        isLoading = false
                    )
                }
                if (selectedId != null) {
                    loadMembersForGroup(selectedId)
                }
            }
        }
    }

    private fun loadMembersForGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMembersLoading = true) }
            memberRepository.refreshMembers(groupId)
            val groupMembers = memberRepository.getMembers(groupId).first()
            val allMembers = buildMemberList(groupMembers)
            _uiState.update { it.copy(members = allMembers, isMembersLoading = false) }
        }
    }

    private fun buildMemberList(groupMembers: List<GroupMember>): List<SplitMember> {
        val me = SplitMember(id = currentUserId, name = "You", isCurrentUser = true)
        val others = groupMembers.map { SplitMember(id = it.userId, name = it.name) }
        return listOf(me) + others
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
        loadMembersForGroup(groupId)
    }

    fun onSplitMethodSelected(method: SplitMethod) {
        _uiState.update { it.copy(splitMethod = method) }
    }

    fun onPaidBySelected(userId: String) {
        _uiState.update { it.copy(paidByUserId = userId) }
    }

    fun onCreateSplit() {
        val state = _uiState.value
        val groupId = state.selectedGroupId ?: return
        if (state.amount.toDoubleOrNull() == null) return

        val desc = state.description.trim().ifBlank { "Expense" }
        val amountCents = (state.amount.toDouble() * 100).toLong()

        when (state.splitMethod) {
            SplitMethod.ByItems -> {
                viewModelScope.launch {
                    _events.send(SplitEvent.GoToAssignItems(groupId, state.amount, desc))
                }
                return
            }
            SplitMethod.ByPercentage -> {
                viewModelScope.launch {
                    _events.send(SplitEvent.GoToSplitByPercentage(groupId, state.amount, desc))
                }
                return
            }
            SplitMethod.Equally -> Unit
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            val allUserIds = state.members.map { it.id }
            val splits = splitEqually(amountCents, allUserIds)
            expensesRepository.createExpenseWithSplits(
                groupId = groupId,
                description = desc,
                amountCents = amountCents,
                currency = "USD",
                splitMethod = "EQUAL",
                splits = splits
            )
            balanceRepository.refreshBalances(groupId)
            groupRepository.refreshGroups()
            activityRepository.refreshActivityFeed()
            _uiState.update { it.copy(isCreating = false) }
            _events.send(SplitEvent.Created)
        }
    }
}
