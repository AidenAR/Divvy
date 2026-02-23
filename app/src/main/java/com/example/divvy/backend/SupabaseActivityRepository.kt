package com.example.divvy.backend

import com.example.divvy.models.ActivityFeedItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseActivityRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ActivityRepository {

    private val _activityFeed = MutableSharedFlow<DataResult<List<ActivityFeedItem>>>(replay = 1).apply {
        tryEmit(DataResult.Loading)
    }

    override fun getGlobalActivityFeed(): Flow<DataResult<List<ActivityFeedItem>>> = _activityFeed

    override suspend fun refreshActivityFeed() {
        try {
            val items = supabaseClient.postgrest
                .rpc("get_global_activity_feed")
                .decodeList<ActivityFeedItem>()
            _activityFeed.emit(DataResult.Success(items))
        } catch (e: Exception) {
            _activityFeed.emit(DataResult.Error("Failed to load activity feed", e))
        }
    }
}
