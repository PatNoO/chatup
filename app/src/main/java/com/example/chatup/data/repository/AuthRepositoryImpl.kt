package com.example.chatup.data.repository

import com.example.chatup.data.source.AuthDataSource
import com.example.chatup.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

// Stub implementation — coroutine-wrapped methods completed in CU-4
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource
) : AuthRepository {

    override fun getCurrentUserId(): String? = authDataSource.getCurrentUser()?.uid

    override fun signOut() = authDataSource.signOut()

    override suspend fun login(email: String, password: String): Result<Unit> = TODO("Implemented in CU-4")

    override suspend fun loginWithGoogle(idToken: String): Result<Unit> = TODO("Implemented in CU-4")

    override suspend fun register(email: String, password: String): Result<Unit> = TODO("Implemented in CU-4")

    override suspend fun sendPasswordReset(email: String): Result<String> = TODO("Implemented in CU-4")
}
