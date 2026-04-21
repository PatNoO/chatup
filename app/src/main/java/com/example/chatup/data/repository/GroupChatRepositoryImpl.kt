package com.example.chatup.data.repository

import com.example.chatup.data.model.ChatMessage
import com.example.chatup.data.source.ChatDataSource
import com.example.chatup.data.source.GroupChatDataSource
import com.example.chatup.domain.repository.GroupChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupChatRepositoryImpl @Inject constructor(
    private val chatDataSource: ChatDataSource,
    private val groupChatDataSource: GroupChatDataSource
) : GroupChatRepository {

    override fun observeGroupMessages(conversationId: String): Flow<List<ChatMessage>> =
        chatDataSource.observeMessagesFlow(conversationId)

    override suspend fun sendGroupMessage(conversationId: String, chatText: String, members: List<String>) =
        groupChatDataSource.sendGroupMessageSuspend(conversationId, chatText)

    override suspend fun createGroupConversation(groupName: String, members: List<String>): String =
        groupChatDataSource.createGroupConversationSuspend(groupName, members)
}
