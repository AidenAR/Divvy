package com.example.divvy.backend

import com.example.divvy.components.GroupIcon
import com.example.divvy.models.Group
import java.util.UUID
import javax.inject.Inject

interface GroupsRepository {
    suspend fun listGroups(): List<Group>
    suspend fun createGroup(name: String, icon: GroupIcon): Group
}

class StubGroupsRepository @Inject constructor() : GroupsRepository {
    private val groups = mutableListOf(
        Group(id = "1", name = "Roommates",    icon = GroupIcon.Home,       memberCount = 3, balanceCents =  16850L),
        Group(id = "2", name = "Weekend Trip", icon = GroupIcon.Flight,     memberCount = 5, balanceCents =  -8730L),
        Group(id = "3", name = "Work Lunch",   icon = GroupIcon.Restaurant, memberCount = 4, balanceCents =   2240L)
    )

    override suspend fun listGroups(): List<Group> = groups.toList()

    override suspend fun createGroup(name: String, icon: GroupIcon): Group {
        val group = Group(id = UUID.randomUUID().toString(), name = name, icon = icon, memberCount = 1)
        groups.add(group)
        return group
    }
}
