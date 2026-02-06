package com.example.divvy.backend

import com.example.divvy.models.Group

interface GroupsRepository {
    suspend fun listGroups(): List<Group>
}

class StubGroupsRepository : GroupsRepository {
    override suspend fun listGroups(): List<Group> = emptyList()
}
