package com.example.divvy.models

import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
data class ExpenseSplit(
    val userId: String,
    val amountCents: Long       // this person's share of the expense
)

@Serializable
data class GroupExpense(
    val id: String,
    val groupId: String,
    val title: String,
    val amountCents: Long,
    val paidByUserId: String,   // who fronted the money
    val splits: List<ExpenseSplit>,
    val createdAt: String       // ISO date string, e.g. "2025-02-18"
)

/**
 * Splits [amountCents] evenly across [userIds].
 * Leftover cents (from integer division) are distributed one per person
 * to the first members in the list.
 */
fun splitEqually(amountCents: Long, userIds: List<String>): List<ExpenseSplit> {
    require(userIds.isNotEmpty()) { "userIds must not be empty" }
    val base      = amountCents / userIds.size
    val remainder = (amountCents % userIds.size).toInt()
    return userIds.mapIndexed { index, userId ->
        ExpenseSplit(userId, if (index < remainder) base + 1 else base)
    }
}

/**
 * Splits [amountCents] according to [percentages], a map of userId → share (0–100).
 * Percentages must sum to 100 (±0.01 tolerance).
 * Rounding uses the largest-remainder method so the splits always total exactly [amountCents].
 */
fun splitByPercentage(amountCents: Long, percentages: Map<String, Double>): List<ExpenseSplit> {
    require(percentages.isNotEmpty()) { "percentages must not be empty" }
    require(abs(percentages.values.sum() - 100.0) < 0.01) { "percentages must sum to 100" }
    val floored   = percentages.mapValues { (_, pct) -> (amountCents * pct / 100.0).toLong() }
    val remainder = amountCents - floored.values.sum()
    // Give the leftover cents to members with the largest fractional parts
    val topByFraction = percentages.keys.sortedByDescending { userId ->
        amountCents * percentages[userId]!! / 100.0 - floored[userId]!!
    }
    val result = floored.toMutableMap()
    topByFraction.take(remainder.toInt()).forEach { userId -> result[userId] = result[userId]!! + 1 }
    return result.map { (userId, amount) -> ExpenseSplit(userId, amount) }
}
