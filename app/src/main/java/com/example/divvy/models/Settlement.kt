package com.example.divvy.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Settlement(
    val id: String = "",
    @SerialName("group_id")     val groupId: String,
    @SerialName("payer_id")     val payerId: String,   // the person who paid/settled
    @SerialName("payee_id")     val payeeId: String,   // the person who was owed
    @SerialName("amount_cents") val amountCents: Long,
    val note: String? = null,
    @SerialName("settled_at")   val settledAt: String = ""
)
