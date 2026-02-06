package com.example.divvy.models

import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String,
    val merchant: String,
    val amountCents: Long,
    val currency: String,
    val createdAt: String
)
