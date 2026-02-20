package com.example.divvy.backend

import com.example.divvy.models.LedgerEntry
import javax.inject.Inject

interface LedgerRepository {
    suspend fun listEntries(): List<LedgerEntry>
}

class StubLedgerRepository @Inject constructor() : LedgerRepository {
    override suspend fun listEntries(): List<LedgerEntry> = emptyList()
}
