package com.example.divvy.backend

import com.example.divvy.models.UserProfile

interface AuthRepository {
    suspend fun getCurrentUser(): UserProfile?
    suspend fun signOut()
}

class StubAuthRepository : AuthRepository {
    override suspend fun getCurrentUser(): UserProfile? = null
    override suspend fun signOut() {}
}
