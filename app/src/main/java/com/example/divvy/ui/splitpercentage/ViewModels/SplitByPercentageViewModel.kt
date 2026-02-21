package com.example.divvy.ui.splitpercentage.ViewModels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divvy.backend.ExpensesRepository
import com.example.divvy.backend.GroupDetailRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PercentageMember(
    val id: String,
    val name: String,
    val color: Color
)

data class SplitByPercentageUiState(
    val description: String = "",
    val amountDisplay: String = "",
    val members: List<PercentageMember> = emptyList(),
    val percentages: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
) {
    val totalPercentage: Double
        get() = percentages.values.sumOf { it.toDoubleOrNull() ?: 0.0 }

    val isValid: Boolean
        get() = kotlin.math.abs(totalPercentage - 100.0) < 0.01

    fun dollarAmountFor(memberId: String): String {
        val total = amountDisplay.toDoubleOrNull() ?: 0.0
        val pct = percentages[memberId]?.toDoubleOrNull() ?: 0.0
        val amount = total * pct / 100.0
        return "$${String.format("%.2f", amount)}"
    }
}

private val MemberColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFFFF9800),
    Color(0xFF7C4DFF),
    Color(0xFF2196F3),
    Color(0xFF00695C),
    Color(0xFFE91E63),
)

@HiltViewModel(assistedFactory = SplitByPercentageViewModel.Factory::class)
class SplitByPercentageViewModel @AssistedInject constructor(
    @Assisted("groupId") private val groupId: String,
    @Assisted("amountDisplay") private val amountDisplay: String,
    @Assisted("description") private val description: String,
    private val groupDetailRepo: GroupDetailRepository,
    private val expensesRepo: ExpensesRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("groupId") groupId: String,
            @Assisted("amountDisplay") amountDisplay: String,
            @Assisted("description") description: String
        ): SplitByPercentageViewModel
    }

    private val _uiState = MutableStateFlow(
        SplitByPercentageUiState(
            description = description.ifBlank { "Expense" },
            amountDisplay = amountDisplay,
            isLoading = true
        )
    )
    val uiState: StateFlow<SplitByPercentageUiState> = _uiState.asStateFlow()

    private val _done = Channel<Unit>(Channel.BUFFERED)
    val done = _done.receiveAsFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val balances = groupDetailRepo.getMemberBalances(groupId)
            val members = mutableListOf(
                PercentageMember("me", "You", MemberColors[0])
            )
            balances.forEachIndexed { i, mb ->
                members.add(
                    PercentageMember(mb.userId, mb.name, MemberColors[(i + 1) % MemberColors.size])
                )
            }
            val equalPct = String.format("%.1f", 100.0 / members.size)
            val defaultPercentages = members.associate { it.id to equalPct }
            _uiState.update {
                it.copy(
                    members = members,
                    percentages = defaultPercentages,
                    isLoading = false
                )
            }
        }
    }

    fun onPercentageChange(memberId: String, value: String) {
        val filtered = value.filter { c -> c.isDigit() || c == '.' }
        val dotCount = filtered.count { it == '.' }
        if (dotCount > 1) return
        val dotIndex = filtered.indexOf('.')
        if (dotIndex != -1 && filtered.length - dotIndex - 1 > 1) return
        _uiState.update { state ->
            state.copy(percentages = state.percentages + (memberId to filtered))
        }
    }

    fun onSplitEvenly() {
        _uiState.update { state ->
            val equalPct = String.format("%.1f", 100.0 / state.members.size)
            state.copy(percentages = state.members.associate { it.id to equalPct })
        }
    }

    fun onDone() {
        val state = _uiState.value
        if (!state.isValid) return
        val amountCents = ((state.amountDisplay.toDoubleOrNull() ?: 0.0) * 100).toLong()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            expensesRepo.createExpense(
                groupId = groupId,
                description = state.description,
                amountCents = amountCents,
                splitMethod = "ByPercentage"
            )
            _uiState.update { it.copy(isSaving = false) }
            _done.send(Unit)
        }
    }
}
