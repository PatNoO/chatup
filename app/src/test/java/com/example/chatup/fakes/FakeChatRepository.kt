package com.example.chatup.fakes

import com.example.chatup.data.model.ChatMessage
import com.example.chatup.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeChatRepository : ChatRepository {

    val messages = MutableStateFlow<List<ChatMessage>>(emptyList())

    var lastSentMessage: String? = null
    var lastSentReceiverId: String? = null

    override fun createConversationId(user2Id: String): String = "conv_$user2Id"

    override fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = messages

    override suspend fun sendMessage(chatText: String, receiverId: String) {
        lastSentMessage = chatText
        lastSentReceiverId = receiverId
    }

    override fun setTyping(conversationId: String, isTyping: Boolean) {}

    override fun observeTyping(conversationId: String, friendId: String): Flow<Boolean> =
        MutableStateFlow(false)

    override suspend fun markPrivateChatDelivered() {}

    override fun markMessagesSeen(conversationId: String) {}
}
