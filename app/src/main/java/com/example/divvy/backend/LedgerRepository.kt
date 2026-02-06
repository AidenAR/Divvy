package com.example.divvy.backend

import com.example.divvy.models.LedgerEntry

interface LedgerRepository {
    suspend fun listEntries(): List<LedgerEntry>
}

class StubLedgerRepository : LedgerRepository {
    override suspend fun listEntries(): List<LedgerEntry> = emptyList()
}
