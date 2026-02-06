package com.example.divvy.models

import kotlinx.serialization.Serializable

@Serializable
data class LedgerEntry(
    val fromUserId: String,
    val toUserId: String,
    val amountCents: Long,
    val currency: String
)
