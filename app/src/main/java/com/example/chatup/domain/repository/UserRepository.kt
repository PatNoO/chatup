package com.example.chatup.domain.repository

import com.example.chatup.data.User

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun loadProfile(uid: String): User?
    suspend fun updateProfile(uid: String, username: String, profileImageUrl: String?)
}
