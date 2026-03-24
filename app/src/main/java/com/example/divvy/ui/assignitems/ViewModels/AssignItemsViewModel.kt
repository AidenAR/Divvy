package com.example.divvy.ui.assignitems.ViewModels

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.ActivityRepository
import com.example.divvy.backend.AuthRepository
import com.example.divvy.backend.BalanceRepository
import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.backend.MemberRepository
import com.example.divvy.backend.ScannedReceiptStore
import com.example.divvy.models.ExpenseSplit
import com.example.divvy.models.ReceiptItemRow
import com.example.divvy.models.splitEqually
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssignMember(
    val id: String,
    val name: String,
    val color: Color
)

data class ReceiptItem(
    val id: String,
    val name: String,
    val priceCents: Long
) {
    val formattedPrice: String
        get() = "$${String.format("%.2f", priceCents / 100.0)}"
}

data class AssignItemsUiState(
    val description: String = "",
    val amountDisplay: String = "",
    val members: List<AssignMember> = emptyList(),
    val items: List<ReceiptItem> = emptyList(),
    val assignments: Map<String, Set<String>> = emptyMap(),
    val expandedItemId: String? = null,
    val paidByUserId: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val coveredBy: Map<String, String> = emptyMap(),
    val expandedCoveringMemberId: String? = null,
)

private val MemberColors = listOf(
    Color(0xFF10B981),
    Color(0xFF3B82F6),
    Color(0xFFF59E0B),
    Color(0xFFF43F5E),
    Color(0xFF8B5CF6),
    Color(0xFF14B8A6),
)

private val fallbackItems = listOf(
    ReceiptItem("i1", "Item 1", 0),
    ReceiptItem("i2", "Item 2", 0),
    ReceiptItem("i3", "Item 3", 0),
)

@HiltViewModel(assistedFactory = AssignItemsViewModel.Factory::class)
class AssignItemsViewModel @AssistedInject constructor(
    @Assisted("groupId") private val groupId: String,
    @Assisted("amountDisplay") private val amountDisplay: String,
    @Assisted("description") private val description: String,
    @Assisted("paidByUserId") private val paidByUserId: String,
    private val authRepository: AuthRepository,
    private val memberRepository: MemberRepository,
    private val expensesRepository: ExpensesRepository,
    private val balanceRepository: BalanceRepository,
    private val groupRepository: GroupRepository,
    private val activityRepository: ActivityRepository,
    private val scannedReceiptStore: ScannedReceiptStore
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("groupId") groupId: String,
            @Assisted("amountDisplay") amountDisplay: String,
            @Assisted("description") description: String,
            @Assisted("paidByUserId") paidByUserId: String
        ): AssignItemsViewModel
    }

    private val _uiState = MutableStateFlow(
        AssignItemsUiState(
            description = description.ifBlank { "Receipt" },
            amountDisplay = amountDisplay,
            paidByUserId = paidByUserId,
            isLoading = true
        )
    )
    val uiState: StateFlow<AssignItemsUiState> = _uiState.asStateFlow()

    private val _done = Channel<Unit>(Channel.BUFFERED)
    val done = _done.receiveAsFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val currentUserId = authRepository.getCurrentUserId()
            memberRepository.refreshMembers(groupId)
            val groupMembers = memberRepository.getMembers(groupId).first()
            val allMembers = mutableListOf(AssignMember(currentUserId, "You", MemberColors[0]))
            groupMembers.forEachIndexed { i, gm ->
                allMembers += AssignMember(gm.userId, gm.name, MemberColors[(i + 1) % MemberColors.size])
            }

            val scannedReceipt = scannedReceiptStore.peek()
            val items = if (scannedReceipt != null && scannedReceipt.items.isNotEmpty()) {
                scannedReceipt.items.map { ReceiptItem(it.id, it.name, it.priceCents) }
            } else {
                fallbackItems
            }

            _uiState.update { it.copy(members = allMembers, items = items, isLoading = false) }
        }
    }

    fun onItemTap(itemId: String) {
        _uiState.update {
            it.copy(expandedItemId = if (it.expandedItemId == itemId) null else itemId)
        }
    }

    fun onToggleMemberForItem(itemId: String, memberId: String) {
        _uiState.update { state ->
            val current = state.assignments[itemId].orEmpty()
            val updated = if (memberId in current) current - memberId else current + memberId
            state.copy(assignments = state.assignments + (itemId to updated))
        }
    }

    fun onToggleCoveringForMember(memberId: String) {
        _uiState.update {
            it.copy(
                expandedCoveringMemberId =
                    if (it.expandedCoveringMemberId == memberId) null else memberId
            )
        }
    }

    fun onSetCovering(coveredUserId: String, covererUserId: String?) {
        _uiState.update { state ->
            val next = if (covererUserId != null) {
                state.coveredBy + (coveredUserId to covererUserId)
            } else {
                state.coveredBy - coveredUserId
            }
            state.copy(coveredBy = next, expandedCoveringMemberId = null)
        }
    }

    fun assignedNamesForItem(itemId: String): String {
        val state = _uiState.value
        val ids = state.assignments[itemId].orEmpty()
        if (ids.isEmpty()) return "Not assigned"
        return state.members.filter { it.id in ids }.joinToString(", ") { it.name }
    }

    fun onNext() {
        val state = _uiState.value

        val perUser = mutableMapOf<String, Long>()
        for (item in state.items) {
            val assignees = state.assignments[item.id].orEmpty()
            if (assignees.isEmpty()) continue
            for (split in splitEqually(item.priceCents, assignees.toList())) {
                perUser[split.userId] = (perUser[split.userId] ?: 0L) + split.amountCents
            }
        }

        val receipt = scannedReceiptStore.peek()
        Log.d("AssignItems", "receipt=${receipt != null}, tax=${receipt?.taxCents}, tip=${receipt?.tipCents}, discount=${receipt?.discountCents}")
        val participantIds = perUser.keys.toList()
        Log.d("AssignItems", "participants=$participantIds, itemTotal=${perUser.values.sum()}")
        if (receipt != null && participantIds.isNotEmpty()) {
            if (receipt.taxCents > 0) {
                for (split in splitEqually(receipt.taxCents, participantIds)) {
                    perUser[split.userId] = (perUser[split.userId] ?: 0L) + split.amountCents
                }
            }
            if (receipt.tipCents > 0) {
                for (split in splitEqually(receipt.tipCents, participantIds)) {
                    perUser[split.userId] = (perUser[split.userId] ?: 0L) + split.amountCents
                }
            }
            if (receipt.discountCents > 0) {
                for (split in splitEqually(receipt.discountCents, participantIds)) {
                    perUser[split.userId] = (perUser[split.userId] ?: 0L) - split.amountCents
                }
            }
        }

        val splits = state.members.map { m ->
            ExpenseSplit(m.id, perUser[m.id] ?: 0L, isCoveredBy = state.coveredBy[m.id])
        }
        val amountCents = splits.sumOf { it.amountCents }
        Log.d("AssignItems", "finalTotal=$amountCents, splits=$splits")

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val groupExpense = expensesRepository.createExpenseWithSplits(
                groupId = groupId,
                description = state.description,
                amountCents = amountCents,
                currency = "USD",
                splitMethod = "BY_ITEM",
                paidByUserId = state.paidByUserId,
                splits = splits
            )
            val receiptRows = state.items.map { item ->
                val assignees = state.assignments[item.id].orEmpty()
                ReceiptItemRow(
                    expenseId = groupExpense.id,
                    description = item.name,
                    priceCents = item.priceCents,
                    assignedUserId = assignees.singleOrNull()
                )
            }.toMutableList()
            if (receipt != null) {
                if (receipt.tipCents > 0) {
                    receiptRows += ReceiptItemRow(groupExpense.id, "Tip", receipt.tipCents)
                }
                if (receipt.discountCents > 0) {
                    receiptRows += ReceiptItemRow(groupExpense.id, "Discount", receipt.discountCents)
                }
            }
            try {
                expensesRepository.saveReceiptItems(receiptRows)
            } catch (e: Exception) {
                Log.e("AssignItems", "Failed to save receipt items", e)
            }
            balanceRepository.refreshBalances(groupId)
            groupRepository.refreshGroups()
            activityRepository.refreshActivityFeed()
            _uiState.update { it.copy(isSaving = false) }
            _done.send(Unit)
        }
    }
}
