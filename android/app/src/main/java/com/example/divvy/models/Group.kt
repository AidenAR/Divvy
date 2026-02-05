package com.example.divvy.models

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val id: String,
    val name: String,
    val memberCount: Int
)
