package com.example.divvy.backend

import com.example.divvy.models.ActivityFeedItem
import kotlinx.coroutines.flow.Flow
import com.example.divvy.backend.DataResult

interface ActivityRepository {
    fun getGlobalActivityFeed(): Flow<DataResult<List<ActivityFeedItem>>>
    suspend fun refreshActivityFeed()
}
