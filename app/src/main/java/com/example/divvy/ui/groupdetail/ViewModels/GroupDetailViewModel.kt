package com.example.divvy.ui.groupdetail.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.ActivityRepository
import com.example.divvy.backend.AuthRepository
import com.example.divvy.backend.BalanceRepository
import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.backend.MemberRepository
import com.example.divvy.backend.ProfilesRepository
import com.example.divvy.components.GroupIcon
import com.example.divvy.models.ActivityItem
import com.example.divvy.models.Group
import com.example.divvy.models.GroupExpense
import com.example.divvy.models.GroupMember
import com.example.divvy.models.MemberBalance
import com.example.divvy.models.ProfileRow
import com.example.divvy.models.SimplifiedPayment
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

enum class SettleMode { Fully, Partially }

data class GroupDetailUiState(
    val group: Group = Group(id = "", name = ""),
    val memberBalances: List<MemberBalance> = emptyList(),
    val simplifiedPayments: List<SimplifiedPayment> = emptyList(),
    val activity: List<ActivityItem> = emptyList(),
    val isLoading: Boolean = true,
    val showManageSheet: Boolean = false,
    val leftGroup: Boolean = false,
    val deletedGroup: Boolean = false,
    val isCreator: Boolean = false,
    val currentMemberIds: Set<String> = emptySet(),
    // Delegate states
    val settlement: SettlementState = SettlementState(),
    val editGroup: EditGroupState = EditGroupState(),
    val inviteMembers: InviteMembersState = InviteMembersState()
) {
    // Convenience accessors so the UI doesn't need to know about delegates
    val expandedMemberId get() = settlement.expandedMemberId
    val expandedCurrency get() = settlement.expandedCurrency
    val settleMode get() = settlement.settleMode
    val settleAmount get() = settlement.settleAmount
    val isSettling get() = settlement.isSettling
    val isEditing get() = editGroup.isEditing
    val editName get() = editGroup.editName
    val editIcon get() = editGroup.editIcon
    val isSavingEdit get() = editGroup.isSaving
    val showInviteSheet get() = inviteMembers.showSheet
    val allProfiles get() = inviteMembers.allProfiles
    val inviteSearchQuery get() = inviteMembers.searchQuery
    val isAddingMember get() = inviteMembers.isAdding
}

@HiltViewModel(assistedFactory = GroupDetailViewModel.Factory::class)
class GroupDetailViewModel @AssistedInject constructor(
    @Assisted private val groupId: String,
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val balanceRepository: BalanceRepository,
    private val expensesRepository: ExpensesRepository,
    private val profilesRepository: ProfilesRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(groupId: String): GroupDetailViewModel
    }

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    private val myUserId: String = authRepository.getCurrentUserId()

    val settlementDelegate = SettlementDelegate(
        groupId = groupId,
        myUserId = myUserId,
        scope = viewModelScope,
        expensesRepository = expensesRepository,
        balanceRepository = balanceRepository,
        groupRepository = groupRepository,
        getMemberBalances = { _uiState.value.memberBalances }
    )

    val editDelegate = EditGroupDelegate(
        groupId = groupId,
        scope = viewModelScope,
        groupRepository = groupRepository
    )

    val inviteDelegate = InviteMembersDelegate(
        groupId = groupId,
        scope = viewModelScope,
        memberRepository = memberRepository,
        profilesRepository = profilesRepository,
        onMemberAdded = { profileId ->
            _uiState.update { it.copy(currentMemberIds = it.currentMemberIds + profileId) }
            viewModelScope.launch {
                groupRepository.refreshGroups()
                activityRepository.refreshActivityFeed()
            }
        }
    )

    init {
        viewModelScope.launch {
            memberRepository.refreshMembers(groupId)
            balanceRepository.refreshBalances(groupId)
            expensesRepository.refreshGroupExpenses(groupId)
        }

        viewModelScope.launch {
            combine(
                groupRepository.getGroup(groupId),
                memberRepository.getMembers(groupId),
                balanceRepository.observeBalances(groupId),
                expensesRepository.observeGroupExpenses(groupId)
            ) { group, members, rawBalances, expenses ->
                // Build display balances from raw backend data — shows what each person
                // directly owes/is owed. Simplified payments are computed separately and
                // used only to determine correct settle direction.
                val simplifiedPaymentsList = simplifyPayments(rawBalances, members, myUserId)

                val memberBalances = members
                    .filter { it.userId != myUserId }
                    .flatMap { member ->
                        val memberRawBalances = rawBalances.filter { it.userId == member.userId }
                        if (memberRawBalances.isEmpty()) {
                            listOf(MemberBalance(userId = member.userId, name = member.name, balanceCents = 0L))
                        } else {
                            memberRawBalances.map { raw ->
                                MemberBalance(
                                    userId = member.userId,
                                    name = member.name,
                                    balanceCents = raw.balanceCents,
                                    currency = raw.currency
                                )
                            }
                        }
                    }
                val activity = buildActivity(expenses, members)
                val memberIds = members.map { it.userId }.toSet() + myUserId
                GroupDetailData(group, memberBalances, simplifiedPaymentsList, activity, memberIds)
            }.collect { data ->
                _uiState.update { current ->
                    current.copy(
                        group = data.group,
                        memberBalances = data.memberBalances,
                        simplifiedPayments = data.simplifiedPayments,
                        activity = data.activity,
                        isLoading = false,
                        isCreator = data.group.createdBy == myUserId,
                        currentMemberIds = data.memberIds
                    )
                }
            }
        }

        viewModelScope.launch {
            settlementDelegate.state.collect { s ->
                _uiState.update { it.copy(settlement = s) }
            }
        }
        viewModelScope.launch {
            editDelegate.state.collect { s ->
                _uiState.update { it.copy(editGroup = s) }
            }
        }
        viewModelScope.launch {
            inviteDelegate.state.collect { s ->
                _uiState.update { it.copy(inviteMembers = s) }
            }
        }
    }

    // --- Settlement (forwarded to delegate) ---
    fun onMemberClick(userId: String, currency: String) =
        settlementDelegate.onMemberClick(userId, currency, _uiState.value.simplifiedPayments)
    fun onSettleModeSelected(mode: SettleMode, currency: String = "USD") = settlementDelegate.onSettleModeSelected(mode, currency)
    fun onSettleAmountChange(value: String) = settlementDelegate.onSettleAmountChange(value)
    fun onConfirmSettle() = settlementDelegate.onConfirmSettle()

    // --- Manage actions ---
    fun onShowManageSheet() {
        _uiState.update { it.copy(showManageSheet = true) }
    }

    fun onDismissManageSheet() {
        _uiState.update { it.copy(showManageSheet = false) }
    }

    fun onLeaveGroup() {
        viewModelScope.launch {
            memberRepository.leaveGroup(groupId)
            groupRepository.refreshGroups()
            activityRepository.refreshActivityFeed()
            _uiState.update { it.copy(leftGroup = true) }
        }
    }

    fun onDeleteGroup() {
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
            groupRepository.refreshGroups()
            activityRepository.refreshActivityFeed()
            _uiState.update { it.copy(deletedGroup = true) }
        }
    }

    // --- Edit group (forwarded to delegate) ---
    fun onStartEdit() = editDelegate.onStartEdit(_uiState.value.group)
    fun onCancelEdit() = editDelegate.onCancelEdit()
    fun onEditNameChange(value: String) = editDelegate.onEditNameChange(value)
    fun onEditIconSelected(icon: GroupIcon) = editDelegate.onEditIconSelected(icon)
    fun onSaveEdit() = editDelegate.onSaveEdit()

    // --- Invite members (forwarded to delegate) ---
    fun onShowInviteSheet() = inviteDelegate.onShowSheet()
    fun onDismissInviteSheet() = inviteDelegate.onDismissSheet()
    fun onInviteSearchChange(value: String) = inviteDelegate.onSearchChange(value)
    fun onInviteMember(profile: ProfileRow) = inviteDelegate.onInviteMember(profile)

    // --- Helpers ---

    private fun buildActivity(
        expenses: List<GroupExpense>,
        members: List<GroupMember>
    ): List<ActivityItem> {
        val memberMap = members.associateBy { it.userId }
        return expenses
            .sortedByDescending { it.createdAt }
            .map { expense ->
                val paidByCurrentUser = expense.paidByUserId == myUserId

                val displayAmountCents = if (paidByCurrentUser) {
                    // I paid: others owe me (total − my effective share).
                    // If my split is covered by someone, my effective share is 0.
                    val myEffectiveShare = expense.splits
                        .find { it.userId == myUserId }
                        ?.let { if (it.isCoveredBy != null) 0L else it.amountCents }
                        ?: 0L
                    expense.amountCents - myEffectiveShare
                } else {
                    // Someone else paid: I owe my own share (0 if covered)
                    // plus any other members' shares that I'm covering.
                    val myDirectShare = expense.splits
                        .find { it.userId == myUserId }
                        ?.let { if (it.isCoveredBy != null) 0L else it.amountCents }
                        ?: 0L
                    val coveringAmount = expense.splits
                        .filter { it.isCoveredBy == myUserId }
                        .sumOf { it.amountCents }
                    myDirectShare + coveringAmount
                }

                ActivityItem(
                    id = expense.id,
                    title = expense.title,
                    amountCents = displayAmountCents,
                    dateLabel = dateLabel(expense.createdAt),
                    paidByLabel = if (paidByCurrentUser) "You"
                    else memberMap[expense.paidByUserId]?.name ?: "Unknown",
                    paidByCurrentUser = paidByCurrentUser,
                    timestamp = expense.createdAt,
                    currency = expense.currency
                )
            }
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

private data class GroupDetailData(
    val group: Group,
    val memberBalances: List<MemberBalance>,
    val simplifiedPayments: List<SimplifiedPayment>,
    val activity: List<ActivityItem>,
    val memberIds: Set<String>
)

/**
 * Splitwise-style debt simplification.
 *
 * The backend returns balances from the current user's perspective:
 *   positive balanceCents = that member owes the current user
 *   negative balanceCents = the current user owes that member
 *
 * The current user never appears as their own row, so we derive their
 * implicit balance per currency as the negated sum of all other rows.
 * Then we run a greedy max-creditor/max-debtor matching loop to produce
 * the minimum number of transactions.
 */
private fun simplifyPayments(
    memberBalances: List<MemberBalance>,
    members: List<GroupMember>,
    myUserId: String
): List<SimplifiedPayment> {
    val nameMap = members.associate { it.userId to it.name }
    val currencies = memberBalances.map { it.currency }.distinct()
    val result = mutableListOf<SimplifiedPayment>()

    for (currency in currencies) {
        val rows = memberBalances.filter { it.currency == currency }

        // Build absolute balance map for every member including the current user.
        // Each row says "member X has balanceCents relative to me":
        //   positive → X owes me  → X is a debtor, I am a creditor by that amount
        //   negative → I owe X    → I am a debtor, X is a creditor by that amount
        val absoluteBalances = LinkedHashMap<String, Long>()
        var myBalance = 0L
        for (mb in rows) {
            // From member X's absolute perspective: they owe (−mb.balanceCents) net
            absoluteBalances[mb.userId] = (absoluteBalances[mb.userId] ?: 0L) - mb.balanceCents
            // Current user's absolute balance accumulates the opposite sign
            myBalance += mb.balanceCents
        }
        if (myBalance != 0L) absoluteBalances[myUserId] = myBalance

        // Split into creditors (net positive = owed money) and debtors (net negative = owe money)
        val creditors = java.util.PriorityQueue<Pair<String, Long>>(compareByDescending { it.second })
        val debtors   = java.util.PriorityQueue<Pair<String, Long>>(compareByDescending { it.second })

        for ((uid, bal) in absoluteBalances) {
            when {
                bal > 0L -> creditors.add(uid to bal)
                bal < 0L -> debtors.add(uid to -bal) // store as positive
            }
        }

        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            val (creditor, credit) = creditors.poll()!!
            val (debtor,  debt)   = debtors.poll()!!

            val amount = minOf(credit, debt)
            result.add(
                SimplifiedPayment(
                    fromUserId        = debtor,
                    fromName          = nameMap[debtor] ?: debtor,
                    toUserId          = creditor,
                    toName            = nameMap[creditor] ?: creditor,
                    amountCents       = amount,
                    currency          = currency,
                    fromIsCurrentUser = debtor == myUserId,
                    toIsCurrentUser   = creditor == myUserId
                )
            )

            val remaining = credit - debt
            when {
                remaining > 0L -> creditors.add(creditor to remaining)
                remaining < 0L -> debtors.add(debtor to -remaining)
            }
        }
    }

    return result.sortedWith(compareBy<SimplifiedPayment> { it.currency }.thenByDescending { it.amountCents })
}