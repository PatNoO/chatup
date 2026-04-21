package com.example.chatup.di

import com.example.chatup.data.repository.AuthRepositoryImpl
import com.example.chatup.data.repository.ChatRepositoryImpl
import com.example.chatup.data.repository.ConversationRepositoryImpl
import com.example.chatup.data.repository.GroupChatRepositoryImpl
import com.example.chatup.data.repository.UserRepositoryImpl
import com.example.chatup.domain.repository.AuthRepository
import com.example.chatup.domain.repository.ChatRepository
import com.example.chatup.domain.repository.ConversationRepository
import com.example.chatup.domain.repository.GroupChatRepository
import com.example.chatup.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds @Singleton
    abstract fun bindConversationRepository(impl: ConversationRepositoryImpl): ConversationRepository

    @Binds @Singleton
    abstract fun bindGroupChatRepository(impl: GroupChatRepositoryImpl): GroupChatRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
