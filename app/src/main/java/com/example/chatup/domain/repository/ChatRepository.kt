package com.example.chatup.domain.repository

import com.example.chatup.data.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun createConversationId(user2Id: String): String
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(chatText: String, receiverId: String)
    fun setTyping(conversationId: String, isTyping: Boolean)
    fun observeTyping(conversationId: String, friendId: String): Flow<Boolean>
    suspend fun markPrivateChatDelivered()
    fun markMessagesSeen(conversationId: String)
}
