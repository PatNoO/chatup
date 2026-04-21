package com.example.chatup.fakes

import com.example.chatup.data.model.ConversationList
import com.example.chatup.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConversationRepository : ConversationRepository {

    val conversationsFlow = MutableStateFlow<List<ConversationList>>(emptyList())

    override fun observeConversations(): Flow<List<ConversationList>> = conversationsFlow
}
