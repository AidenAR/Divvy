package com.example.divvy.backend

import com.example.divvy.models.Settlement
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

interface SettlementsRepository {
    suspend fun createSettlement(
        groupId: String,
        payerId: String,
        payeeId: String,
        amountCents: Long,
        note: String? = null
    ): Settlement

    suspend fun listSettlementsByGroup(groupId: String): List<Settlement>
    suspend fun listAllSettlements(): List<Settlement>
}

@Singleton
class SupabaseSettlementsRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : SettlementsRepository {

    override suspend fun createSettlement(
        groupId: String,
        payerId: String,
        payeeId: String,
        amountCents: Long,
        note: String?
    ): Settlement {
        val row = Settlement(
            groupId = groupId,
            payerId = payerId,
            payeeId = payeeId,
            amountCents = amountCents,
            note = note
        )
        // Insert the row and ask Supabase to return the full row (including generated id/settled_at)
        return supabaseClient.from("settlements")
            .insert(row) { select() }
            .decodeSingle()
    }

    override suspend fun listSettlementsByGroup(groupId: String): List<Settlement> =
        supabaseClient.from("settlements")
            .select { filter { eq("group_id", groupId) } }
            .decodeList()

    override suspend fun listAllSettlements(): List<Settlement> =
        supabaseClient.from("settlements")
            .select()
            .decodeList()
}
