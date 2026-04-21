package com.example.chatup.data.source

interface AuthDataSourceContract {
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    fun signOut()
    suspend fun loginSuspend(email: String, password: String): Result<Unit>
    suspend fun loginWithGoogleSuspend(idToken: String): Result<Unit>
    suspend fun registerSuspend(email: String, password: String): Result<Unit>
    suspend fun sendPasswordResetSuspend(email: String): Result<String>
}
