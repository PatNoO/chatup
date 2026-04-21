package com.example.chatup.domain.repository

import com.example.chatup.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface GroupChatRepository {
    fun observeGroupMessages(conversationId: String): Flow<List<ChatMessage>>
    suspend fun sendGroupMessage(conversationId: String, chatText: String, members: List<String>)
    suspend fun createGroupConversation(groupName: String, members: List<String>): String
}
