package com.example.divvy.models

import kotlinx.serialization.Serializable

@Serializable
data class ParsedTransaction(
    val date: String,
    val description: String,
    val amountCents: Long,
    val category: String? = null,
    val isCredit: Boolean = false
)

enum class TransactionStatus {
    Pending,
    Added,
    Skipped
}
