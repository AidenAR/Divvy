package com.example.divvy.models

import com.example.divvy.components.GroupIcon
import kotlinx.serialization.Serializable

@Serializable
data class CurrencyBalance(
    val currency: String = "USD",
    val balanceCents: Long = 0L
)

@Serializable
data class Group(
    val id: String,
    val name: String,
    val emoji: String = "",
    val icon: GroupIcon = GroupIcon.Group,
    val memberCount: Int = 0,
    val balances: List<CurrencyBalance> = emptyList(),
    val currency: String = "USD",
    val createdBy: String = ""
) {
    /** Backward-compat: sum of all currency balances */
    val balanceCents: Long get() = balances.sumOf { it.balanceCents }

    /** Positive = you are owed, negative = you owe */
    val isOwed: Boolean get() = balanceCents >= 0

    val formattedBalance: String
        get() {
            if (balances.isEmpty()) return formatAmount(0L, "USD")
            return balances.filter { it.balanceCents != 0L }
                .joinToString(" + ") { formatAmount(it.balanceCents, it.currency) }
                .ifEmpty { formatAmount(0L, "USD") }
        }

    val balanceLabel: String
        get() = if (isOwed) "You are owed ${formattedBalance}" else "You owe ${formattedBalance}"
}
