package com.example.chatup.fakes

import com.example.chatup.data.source.AuthDataSourceContract

class FakeAuthDataSource : AuthDataSourceContract {

    var registerShouldSucceed = true
    var loginShouldSucceed = true
    var stubbedUserId: String? = null
    var stubbedUserEmail: String? = null

    override fun getCurrentUserId(): String? = stubbedUserId
    override fun getCurrentUserEmail(): String? = stubbedUserEmail
    override fun signOut() {}

    override suspend fun loginSuspend(email: String, password: String): Result<Unit> =
        if (loginShouldSucceed) Result.success(Unit)
        else Result.failure(Exception("Invalid credentials"))

    override suspend fun loginWithGoogleSuspend(idToken: String): Result<Unit> =
        if (loginShouldSucceed) Result.success(Unit)
        else Result.failure(Exception("Google login failed"))

    override suspend fun registerSuspend(email: String, password: String): Result<Unit> =
        if (registerShouldSucceed) Result.success(Unit)
        else Result.failure(Exception("Registration failed"))

    override suspend fun sendPasswordResetSuspend(email: String): Result<String> =
        if (loginShouldSucceed) Result.success(email)
        else Result.failure(Exception("Reset failed"))
}
