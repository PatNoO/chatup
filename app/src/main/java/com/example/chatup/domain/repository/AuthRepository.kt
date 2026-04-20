package com.example.chatup.domain.repository

interface AuthRepository {
    fun getCurrentUserId(): String?
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun loginWithGoogle(idToken: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<String>
    fun signOut()
}
