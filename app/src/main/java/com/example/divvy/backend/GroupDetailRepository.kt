package com.example.divvy.backend

import com.example.divvy.components.GroupIcon
import com.example.divvy.models.ActivityItem
import com.example.divvy.models.Group
import com.example.divvy.models.MemberBalance
import javax.inject.Inject

interface GroupDetailRepository {
    suspend fun getGroup(groupId: String): Group
    suspend fun getMemberBalances(groupId: String): List<MemberBalance>
    suspend fun getActivity(groupId: String): List<ActivityItem>
}

class StubGroupDetailRepository @Inject constructor() : GroupDetailRepository {

    override suspend fun getGroup(groupId: String): Group = groups[groupId]
        ?: Group(id = groupId, name = "Group", icon = GroupIcon.Group)

    override suspend fun getMemberBalances(groupId: String): List<MemberBalance> =
        memberBalances[groupId] ?: emptyList()

    override suspend fun getActivity(groupId: String): List<ActivityItem> =
        activity[groupId] ?: emptyList()

    private val groups = mapOf(
        "1" to Group(id = "1", name = "Roommates",    icon = GroupIcon.Home,
            memberCount = 3, balanceCents =  14550L),
        "2" to Group(id = "2", name = "Weekend Trip", icon = GroupIcon.Flight,
            memberCount = 5, balanceCents = -8730L),
        "3" to Group(id = "3", name = "Work Lunch",   icon =
            GroupIcon.Restaurant, memberCount = 4, balanceCents =  2240L)
    )

    private val memberBalances = mapOf(
        "1" to listOf(
            MemberBalance("u1", "Sarah",  6750L),
            MemberBalance("u2", "Mike",   7800L),
            MemberBalance("u3", "Alex",  -1230L)
        ),
        "2" to listOf(
            MemberBalance("u4", "Jordan", -3400L),
            MemberBalance("u5", "Taylor",  2100L),
            MemberBalance("u6", "Casey",  -7430L)
        ),
        "3" to listOf(
            MemberBalance("u7", "Priya",  1200L),
            MemberBalance("u8", "Devon",  1040L)
        )
    )

    private val activity = mapOf(
        "1" to listOf(
            ActivityItem("a1", "Whole Foods",      20000L, "Today",     "You",
                paidByCurrentUser = true),
            ActivityItem("a2", "Uber to Airport",   4550L, "Yesterday", "Sarah",
                paidByCurrentUser = false),
            ActivityItem("a3", "Electric Bill",    12300L, "Mon",       "You",
                paidByCurrentUser = true)
        ),
        "2" to listOf(
            ActivityItem("a4", "Hotel",            45000L, "Fri",
                "Jordan", paidByCurrentUser = false),
            ActivityItem("a5", "Dinner",            8900L, "Fri",       "You",
                paidByCurrentUser = true)
        ),
        "3" to listOf(
            ActivityItem("a6", "Sushi Palace",      6200L, "Today",     "You",
                paidByCurrentUser = true),
            ActivityItem("a7", "Coffee Run",        1800L, "Yesterday", "Priya",
                paidByCurrentUser = false)
        )
    )
}
