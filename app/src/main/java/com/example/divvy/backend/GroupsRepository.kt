package com.example.divvy.backend

import com.example.divvy.models.Group
import javax.inject.Inject

interface GroupsRepository {
    suspend fun listGroups(): List<Group>
}

class StubGroupsRepository @Inject constructor() : GroupsRepository {
    override suspend fun listGroups(): List<Group> = listOf(
        Group(id = "1", name = "Roommates", iconName = "Home", memberCount = 3, balanceCents = 16850L),
        Group(id = "2", name = "Weekend Trip", iconName = "Flight", memberCount = 5, balanceCents = -8730L),
        Group(id = "3", name = "Work Lunch", iconName = "Restaurant", memberCount = 4, balanceCents = 2240L)
    )
}
