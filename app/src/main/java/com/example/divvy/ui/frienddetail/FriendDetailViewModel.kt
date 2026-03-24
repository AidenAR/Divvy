package com.example.divvy.ui.frienddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.AuthRepository
import com.example.divvy.backend.DataResult
import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.FriendsRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.backend.MemberRepository
import com.example.divvy.components.GroupIcon
import com.example.divvy.models.FriendActivityItem
import com.example.divvy.models.GroupBalance
import com.example.divvy.models.GroupExpense
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class FriendDetailUiState(
    val friendName: String = "",
    val balances: List<GroupBalance> = emptyList(),
    val activity: List<FriendActivityItem> = emptyList(),
    val isLoading: Boolean = true,
    val navigateToSplitWithGroupId: String? = null
)

@HiltViewModel(assistedFactory = FriendDetailViewModel.Factory::class)
class FriendDetailViewModel @AssistedInject constructor(
    @Assisted private val friendUserId: String,
    private val authRepository: AuthRepository,
    private val friendsRepository: FriendsRepository,
    private val expensesRepository: ExpensesRepository,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(friendUserId: String): FriendDetailViewModel
    }

    private val _uiState = MutableStateFlow(FriendDetailUiState())
    val uiState: StateFlow<FriendDetailUiState> = _uiState.asStateFlow()

    private val myUserId: String = authRepository.getCurrentUserId()
    private var friendFirstName: String = ""
    private var sharedGroupIds: List<String> = emptyList()

    init {
        viewModelScope.launch {
            // Fetch friend balances and group info in parallel
            val friendBalances = friendsRepository.getFriendsBalances()
            val friend = friendBalances.find { it.userId == friendUserId }

            val friendName = friend?.displayName ?: "Friend"
            friendFirstName = friend?.firstName.orEmpty().ifBlank { "Friend" }
            val balances = friend?.groupBalances ?: emptyList()
            sharedGroupIds = balances.map { it.groupId }.distinct()

            // Build group info map from friend balances + all groups for fallback
            val groupInfoFromBalances = balances.associateBy { it.groupId }

            _uiState.update {
                it.copy(
                    friendName = friendName,
                    balances = balances.filter { gb -> gb.balanceCents != 0L }
                )
            }

            // Fetch all expenses and build activity
            expensesRepository.refreshAllExpenses()
            expensesRepository.observeAllGroupExpenses().collect { allExpenses ->
                val activity = buildFriendActivity(allExpenses, groupInfoFromBalances)
                _uiState.update {
                    it.copy(activity = activity, isLoading = false)
                }
            }
        }
    }

    fun onAddExpense() {
        viewModelScope.launch {
            // Collect all group IDs to check: from balances + all user groups
            val allGroupIds = sharedGroupIds.toMutableSet()
            try {
                groupRepository.refreshGroups()
                val groups = groupRepository.listGroups().first { it is DataResult.Success }
                if (groups is DataResult.Success) {
                    allGroupIds.addAll(groups.data.map { it.id })
                }
            } catch (_: Exception) { }

            // Look for an existing 1-on-1 group (exactly 2 members: me + friend)
            var existing1on1GroupId: String? = null
            for (groupId in allGroupIds) {
                try {
                    memberRepository.refreshMembers(groupId)
                    val members = memberRepository.getMembers(groupId).first()
                    if (members.size == 2 && members.any { it.userId == friendUserId }) {
                        existing1on1GroupId = groupId
                        break
                    }
                } catch (_: Exception) { }
            }

            if (existing1on1GroupId != null) {
                _uiState.update { it.copy(navigateToSplitWithGroupId = existing1on1GroupId) }
            } else {
                // Create a new 1-on-1 group
                try {
                    val groupName = "$friendFirstName and You"
                    val group = groupRepository.createGroup(groupName, GroupIcon.Group)
                    memberRepository.addMember(group.id, friendUserId)
                    groupRepository.refreshGroups()
                    _uiState.update { it.copy(navigateToSplitWithGroupId = group.id) }
                } catch (_: Exception) { }
            }
        }
    }

    fun onNavigateToSplitHandled() {
        _uiState.update { it.copy(navigateToSplitWithGroupId = null) }
    }

    private suspend fun buildFriendActivity(
        allExpenses: List<GroupExpense>,
        groupInfoFromBalances: Map<String, GroupBalance>
    ): List<FriendActivityItem> {
        // Build a group lookup map, using balances first, then falling back to GroupRepository
        val groupInfoMap = groupInfoFromBalances.toMutableMap()
        val missingGroupIds = mutableSetOf<String>()

        // Filter expenses where both users have splits
        val relevantExpenses = allExpenses.filter { expense ->
            val hasMe = expense.splits.any { it.userId == myUserId } || expense.paidByUserId == myUserId
            val hasFriend = expense.splits.any { it.userId == friendUserId } || expense.paidByUserId == friendUserId
            hasMe && hasFriend
        }

        // Collect missing group IDs
        relevantExpenses.forEach { expense ->
            if (expense.groupId !in groupInfoMap) {
                missingGroupIds.add(expense.groupId)
            }
        }

        // Fetch missing group info
        if (missingGroupIds.isNotEmpty()) {
            try {
                groupRepository.refreshGroups()
                // Collect once from the flow to get current groups
                val groups = groupRepository.listGroups().first { it is DataResult.Success }
                if (groups is DataResult.Success) {
                    groups.data.forEach { group ->
                        if (group.id in missingGroupIds) {
                            groupInfoMap[group.id] = GroupBalance(
                                groupId = group.id,
                                groupName = group.name,
                                groupIcon = group.icon.name,
                                balanceCents = 0L,
                                currency = "USD"
                            )
                        }
                    }
                }
            } catch (_: Exception) { }
        }

        return relevantExpenses
            .mapNotNull { expense -> buildActivityItem(expense, groupInfoMap) }
            .sortedByDescending { it.timestamp }
    }

    private fun buildActivityItem(
        expense: GroupExpense,
        groupInfoMap: Map<String, GroupBalance>
    ): FriendActivityItem? {
        val paidByMe = expense.paidByUserId == myUserId
        val paidByFriend = expense.paidByUserId == friendUserId

        // Only show expenses where one of the two users is the payer
        if (!paidByMe && !paidByFriend) return null

        val displayAmountCents = if (paidByMe) {
            // I paid: show how much the friend owes me (their split)
            expense.splits.find { it.userId == friendUserId }
                ?.let { if (it.isCoveredBy != null) 0L else it.amountCents }
                ?: 0L
        } else {
            // Friend paid: show how much I owe them (my split)
            expense.splits.find { it.userId == myUserId }
                ?.let { if (it.isCoveredBy != null) 0L else it.amountCents }
                ?: 0L
        }

        // Skip zero-impact expenses
        if (displayAmountCents == 0L) return null

        val groupInfo = groupInfoMap[expense.groupId]

        return FriendActivityItem(
            id = expense.id,
            title = expense.title,
            amountCents = displayAmountCents,
            dateLabel = dateLabel(expense.createdAt),
            paidByCurrentUser = paidByMe,
            timestamp = expense.createdAt,
            currency = expense.currency,
            groupId = expense.groupId,
            groupName = groupInfo?.groupName ?: "Unknown Group",
            groupIcon = groupInfo?.groupIcon
        )
    }

    private fun dateLabel(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate.take(10))
            val today = LocalDate.now()
            when (date) {
                today -> "Today"
                today.minusDays(1) -> "Yesterday"
                else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            }
        } catch (_: Exception) { isoDate }
    }
}
