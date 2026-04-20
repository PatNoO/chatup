package com.example.chatup.data.repository

import com.example.chatup.data.ChatMessage
import com.example.chatup.data.source.ChatDataSource
import com.example.chatup.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDataSource: ChatDataSource
) : ChatRepository {

    override fun createConversationId(user2Id: String): String =
        chatDataSource.createConversationId(user2Id)

    override fun observeMessages(conversationId: String): Flow<List<ChatMessage>> =
        chatDataSource.observeMessagesFlow(conversationId)

    override suspend fun sendMessage(chatText: String, receiverId: String) =
        chatDataSource.sendMessageSuspend(chatText, receiverId)

    override fun setTyping(conversationId: String, isTyping: Boolean) =
        chatDataSource.setTyping(conversationId, isTyping)

    override fun observeTyping(conversationId: String, friendId: String): Flow<Boolean> =
        chatDataSource.observeTypingFlow(conversationId, friendId)

    override suspend fun markPrivateChatDelivered() =
        chatDataSource.markPrivateChatDeliveredSuspend()

    override fun markMessagesSeen(conversationId: String) =
        chatDataSource.markMessagesSeen(conversationId)
}
