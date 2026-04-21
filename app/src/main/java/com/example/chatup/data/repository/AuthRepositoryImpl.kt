package com.example.chatup.data.repository

import com.example.chatup.data.source.AuthDataSourceContract
import com.example.chatup.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSourceContract
) : AuthRepository {

    override fun getCurrentUserId(): String? = authDataSource.getCurrentUserId()

    override fun getCurrentUserEmail(): String? = authDataSource.getCurrentUserEmail()

    override fun signOut() = authDataSource.signOut()

    override suspend fun login(email: String, password: String): Result<Unit> =
        authDataSource.loginSuspend(email, password)

    override suspend fun loginWithGoogle(idToken: String): Result<Unit> =
        authDataSource.loginWithGoogleSuspend(idToken)

    override suspend fun register(email: String, password: String): Result<Unit> =
        authDataSource.registerSuspend(email, password)

    override suspend fun sendPasswordReset(email: String): Result<String> =
        authDataSource.sendPasswordResetSuspend(email)
}
