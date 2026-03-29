package com.example.divvy.ui.ledger.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.AuthRepository
import com.example.divvy.backend.DataResult
import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.backend.MemberRepository
import com.example.divvy.backend.SettlementsRepository
import com.example.divvy.models.Group
import com.example.divvy.models.LedgerEntry
import com.example.divvy.models.LedgerEntryType
import com.example.divvy.models.formatAmount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

enum class LedgerFilter { ALL, EXPENSES, SETTLEMENTS }

data class LedgerUiState(
    val allEntries: List<LedgerEntry> = emptyList(),
    val filteredEntries: List<LedgerEntry> = emptyList(),
    val filter: LedgerFilter = LedgerFilter.ALL,
    val selectedGroupId: String? = null,
    val groupOptions: List<Group> = emptyList(),
    val isLoading: Boolean = true
) {
    val netBalanceByCurrency: Map<String, Long>
        get() {
            val byCurrency = mutableMapOf<String, Long>()
            for (entry in allEntries.filter { it.type == LedgerEntryType.EXPENSE }) {
                val delta = if (entry.paidByCurrentUser) entry.amountCents else -entry.amountCents
                byCurrency[entry.currency] = (byCurrency[entry.currency] ?: 0L) + delta
            }
            return byCurrency
        }

    val netBalanceCents: Long
        get() = netBalanceByCurrency.values.sum()

    val formattedNetBalance: String
        get() {
            val byCurrency = netBalanceByCurrency
            if (byCurrency.isEmpty()) return formatAmount(0L, "USD")
            return byCurrency.entries
                .filter { it.value != 0L }
                .joinToString(", ") { formatAmount(kotlin.math.abs(it.value), it.key) }
                .ifEmpty { formatAmount(0L, "USD") }
        }

    val isNetPositive: Boolean get() = netBalanceCents >= 0
}

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val memberRepository: MemberRepository,
    private val expensesRepository: ExpensesRepository,
    private val settlementsRepository: SettlementsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()

    private val myUserId = authRepository.getCurrentUserId()

    init {
        viewModelScope.launch { expensesRepository.refreshAllExpenses() }

        viewModelScope.launch {
            combine(
                groupRepository.listGroups(),
                expensesRepository.observeAllGroupExpenses()
            ) { groupsResult, allExpenses ->
                groupsResult to allExpenses
            }.collect { (groupsResult, allExpenses) ->
                val groups = (groupsResult as? DataResult.Success)?.data ?: return@collect
                val groupNameMap = groups.associate { it.id to it.name }

                val memberNameMap = mutableMapOf<String, String>()
                memberNameMap[myUserId] = "You"
                coroutineScope {
                    groups.map { group ->
                        async {
                            memberRepository.refreshMembers(group.id)
                            memberRepository.getMembers(group.id).first()
                        }
                    }.awaitAll().flatten().forEach { member ->
                        memberNameMap[member.userId] = member.name
                    }
                }

                // Expenses (excluding any old "Settlement" expenses written before the fix)
                val expenseEntries = allExpenses
                    .filter { it.splitMethod != "SETTLEMENT" }
                    .map { expense ->
                        LedgerEntry(
                            id = expense.id,
                            type = LedgerEntryType.EXPENSE,
                            title = expense.title,
                            amountCents = expense.amountCents,
                            groupId = expense.groupId,
                            paidByUserId = expense.paidByUserId,
                            dateLabel = dateLabel(expense.createdAt),
                            groupName = groupNameMap[expense.groupId] ?: "",
                            paidByName = memberNameMap[expense.paidByUserId] ?: "Unknown",
                            paidByCurrentUser = expense.paidByUserId == myUserId,
                            currency = expense.currency
                        )
                    }

                // Settlements from the dedicated table
                val allSettlements = try {
                    settlementsRepository.listAllSettlements()
                } catch (_: Exception) {
                    emptyList()
                }
                val settlementEntries = allSettlements
                    .filter { groupNameMap.containsKey(it.groupId) }
                    .map { s ->
                        LedgerEntry(
                            id = s.id,
                            type = LedgerEntryType.SETTLEMENT,
                            title = "Settlement",
                            amountCents = s.amountCents,
                            groupId = s.groupId,
                            paidByUserId = s.payerId,
                            dateLabel = dateLabel(s.settledAt),
                            toUserId = s.payeeId,
                            groupName = groupNameMap[s.groupId] ?: "",
                            paidByName = memberNameMap[s.payerId] ?: "Unknown",
                            toName = memberNameMap[s.payeeId] ?: "Unknown",
                            paidByCurrentUser = s.payerId == myUserId,
                            currency = "USD"
                        )
                    }

                val entries = (expenseEntries + settlementEntries)
                    .sortedByDescending { it.dateLabel }

                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    allEntries = entries,
                    filteredEntries = applyFilters(entries, currentState.filter, currentState.selectedGroupId),
                    groupOptions = groups,
                    isLoading = false
                )
            }
        }
    }

    fun onFilterSelected(filter: LedgerFilter) {
        _uiState.update { state ->
            state.copy(
                filter = filter,
                filteredEntries = applyFilters(state.allEntries, filter, state.selectedGroupId)
            )
        }
    }

    fun onGroupSelected(groupId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedGroupId = groupId,
                filteredEntries = applyFilters(state.allEntries, state.filter, groupId)
            )
        }
    }

    private fun applyFilters(
        entries: List<LedgerEntry>,
        filter: LedgerFilter,
        groupId: String?
    ): List<LedgerEntry> {
        var result = entries
        when (filter) {
            LedgerFilter.EXPENSES -> result = result.filter { it.type == LedgerEntryType.EXPENSE }
            LedgerFilter.SETTLEMENTS -> result = result.filter { it.type == LedgerEntryType.SETTLEMENT }
            LedgerFilter.ALL -> {}
        }
        if (groupId != null) {
            result = result.filter { it.groupId == groupId }
        }
        return result
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
        } catch (_: Exception) {
            isoDate
        }
    }
}
