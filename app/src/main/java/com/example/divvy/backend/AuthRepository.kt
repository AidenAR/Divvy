package com.example.divvy.backend

import com.example.divvy.models.UserProfile
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.OTP

interface AuthRepository {
    suspend fun getCurrentUser(): UserProfile?
    suspend fun signOut()
    suspend fun signInEmail(email: String, password: String)
    suspend fun signUpEmail(email: String, password: String)
    suspend fun signInWithGoogle()
    suspend fun sendPhoneOtp(phone: String)
    suspend fun verifyPhoneOtp(phone: String, token: String)
}

class StubAuthRepository : AuthRepository {
    override suspend fun getCurrentUser(): UserProfile? = null
    override suspend fun signOut() {}
    override suspend fun signInEmail(email: String, password: String) {}
    override suspend fun signUpEmail(email: String, password: String) {}
    override suspend fun signInWithGoogle() {}
    override suspend fun sendPhoneOtp(phone: String) {}
    override suspend fun verifyPhoneOtp(phone: String, token: String) {}
}

class SupabaseAuthRepository : AuthRepository {
    private val client
        get() = SupabaseClientProvider.client

    override suspend fun getCurrentUser(): UserProfile? {
        val session = client.auth.currentSessionOrNull() ?: return null
        val user = session.user ?: client.auth.retrieveUserForCurrentSession(updateSession = true)
        val displayName = user.email ?: user.phone ?: "User"
        return UserProfile(
            id = user.id,
            displayName = displayName,
            email = user.email
        )
    }

    override suspend fun signOut() {
        client.auth.signOut()
    }

    override suspend fun signInEmail(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUpEmail(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signInWithGoogle() {
        client.auth.signInWith(Google)
    }

    override suspend fun sendPhoneOtp(phone: String) {
        client.auth.signInWith(OTP) {
            this.phone = phone
            createUser = true
        }
    }

    override suspend fun verifyPhoneOtp(phone: String, token: String) {
        client.auth.verifyPhoneOtp(OtpType.Phone.SMS, phone, token)
    }
}
