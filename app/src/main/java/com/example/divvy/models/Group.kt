package com.example.divvy.models

import com.example.divvy.components.GroupIcon
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String,
    val name: String,
    val emoji: String = "",
    val icon: GroupIcon = GroupIcon.Group,
    val memberCount: Int = 0,
    val balanceCents: Long = 0L,
    val currency: String = "USD"
) {
    /** Positive = you are owed, negative = you owe */
    val isOwed: Boolean get() = balanceCents >= 0

    val formattedBalance: String
        get() {
            val dollars = kotlin.math.abs(balanceCents) / 100.0
            return "$${String.format("%.2f", dollars)}"
        }

    val balanceLabel: String
        get() = if (isOwed) "You are owed ${formattedBalance}" else "You owe ${formattedBalance}"
}
