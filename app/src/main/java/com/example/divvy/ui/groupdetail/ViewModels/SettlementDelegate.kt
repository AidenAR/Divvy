package com.example.divvy.ui.groupdetail.ViewModels

import com.example.divvy.backend.BalanceRepository
import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.GroupRepository
import com.example.divvy.models.ExpenseSplit
import com.example.divvy.models.MemberBalance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettlementState(
    val expandedMemberId: String? = null,
    val expandedCurrency: String? = null,
    val settleMode: SettleMode? = null,
    val settleAmount: String = "",
    val isSettling: Boolean = false,
    val currency: String = "USD"
)

class SettlementDelegate(
    private val groupId: String,
    private val myUserId: String,
    private val scope: CoroutineScope,
    private val expensesRepository: ExpensesRepository,
    private val balanceRepository: BalanceRepository,
    private val groupRepository: GroupRepository,
    private val getMemberBalances: () -> List<MemberBalance>
) {
    private val _state = MutableStateFlow(SettlementState())
    val state: StateFlow<SettlementState> = _state.asStateFlow()

    fun onMemberClick(userId: String, currency: String) {
        _state.update { s ->
            if (s.expandedMemberId == userId && s.expandedCurrency == currency)
                s.copy(expandedMemberId = null, expandedCurrency = null, settleMode = null, settleAmount = "")
            else
                s.copy(expandedMemberId = userId, expandedCurrency = currency, settleMode = null, settleAmount = "", currency = currency)
        }
    }

    fun onSettleModeSelected(mode: SettleMode, currency: String = "USD") {
        _state.update { s ->
            val balance = getMemberBalances()
                .find { it.userId == s.expandedMemberId && it.currency == currency }?.balanceCents ?: 0L
            val amount = if (mode == SettleMode.Fully) {
                String.format("%.2f", kotlin.math.abs(balance) / 100.0)
            } else ""
            s.copy(settleMode = mode, settleAmount = amount, currency = currency)
        }
    }

    fun onSettleAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } > 1) return
        val dot = filtered.indexOf('.')
        if (dot != -1 && filtered.length - dot - 1 > 2) return
        _state.update { it.copy(settleAmount = filtered) }
    }

    fun onConfirmSettle(userId: String) {
        val s = _state.value
        val amountCents = (s.settleAmount.toDoubleOrNull() ?: return).let {
            (it * 100).toLong()
        }
        if (amountCents <= 0) return
        val balance = getMemberBalances()
            .find { it.userId == userId && it.currency == s.currency }?.balanceCents ?: return

        val (paidBy, splitUserId) = if (balance < 0)
            Pair(myUserId, userId) else Pair(userId, myUserId)

        scope.launch {
            _state.update { it.copy(isSettling = true) }
            expensesRepository.createExpenseWithSplits(
                groupId = groupId,
                description = "Settlement",
                amountCents = amountCents,
                currency = s.currency,
                splitMethod = "SETTLEMENT",
                paidByUserId = paidBy,
                splits = listOf(ExpenseSplit(splitUserId, amountCents))
            )
            balanceRepository.refreshBalances(groupId)
            groupRepository.refreshGroups()
            _state.update {
                SettlementState()
            }
        }
    }
}
