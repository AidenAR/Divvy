package com.example.divvy.backend

import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushTokenRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthRepository
) {
    suspend fun syncToken() {
        val token = FirebaseMessaging.getInstance().token.await()
        val userId = authRepository.getCurrentUserId()
        supabaseClient.postgrest["push_tokens"].upsert(
            mapOf("user_id" to userId, "token" to token)
        )
    }
}
