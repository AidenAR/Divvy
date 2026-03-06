package com.example.divvy.backend

import com.example.divvy.models.ParsedTransaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionHolder @Inject constructor() {
    var transactions: List<ParsedTransaction> = emptyList()
}
