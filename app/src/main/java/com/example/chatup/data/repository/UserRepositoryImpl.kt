package com.example.chatup.data.repository

import com.example.chatup.data.User
import com.example.chatup.data.source.UserDataSource
import com.example.chatup.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDataSource: UserDataSource
) : UserRepository {

    override suspend fun getUsers(): List<User> =
        userDataSource.getAllUsersExceptCurrent()

    override suspend fun loadProfile(uid: String): User? =
        userDataSource.loadProfileSuspend(uid)

    override suspend fun updateProfile(uid: String, username: String, profileImageUrl: String?) =
        userDataSource.updateProfileSuspend(uid, username, profileImageUrl)
}
