package com.example.chatup.domain.repository

import com.example.chatup.data.model.ConversationList
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun observeConversations(): Flow<List<ConversationList>>
}
