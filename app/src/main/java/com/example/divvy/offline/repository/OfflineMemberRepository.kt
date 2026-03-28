package com.example.divvy.offline.repository

import com.example.divvy.backend.MemberRepository
import com.example.divvy.backend.SupabaseMemberRepository
import com.example.divvy.models.GroupMember
import com.example.divvy.offline.NetworkMonitor
import com.example.divvy.offline.db.dao.CachedMemberDao
import com.example.divvy.offline.db.entity.CachedMemberEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineMemberRepository @Inject constructor(
    private val remote: SupabaseMemberRepository,
    private val memberDao: CachedMemberDao,
    private val networkMonitor: NetworkMonitor
) : MemberRepository {

    override fun getMembers(groupId: String): Flow<List<GroupMember>> =
        memberDao.getByGroupId(groupId).map { entities ->
            entities.map { GroupMember(userId = it.userId, name = it.name) }
        }

    override suspend fun addMember(groupId: String, userId: String) {
        remote.addMember(groupId, userId)
        refreshMembers(groupId)
    }

    override suspend fun leaveGroup(groupId: String) {
        remote.leaveGroup(groupId)
        memberDao.deleteByGroupId(groupId)
    }

    override suspend fun refreshMembers(groupId: String) {
        if (!networkMonitor.isOnline.value) return
        try {
            remote.refreshMembers(groupId)
            // Collect the latest from remote and cache
            remote.getMembers(groupId).collect { members ->
                memberDao.deleteByGroupId(groupId)
                memberDao.insertAll(members.map { CachedMemberEntity(groupId, it.userId, it.name) })
                return@collect
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to refresh members for group $groupId")
        }
    }

    override fun clearCache(groupId: String) {
        remote.clearCache(groupId)
    }
}
