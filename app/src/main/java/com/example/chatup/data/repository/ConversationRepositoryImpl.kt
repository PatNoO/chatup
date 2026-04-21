package com.example.chatup.data.repository

import android.util.Log
import com.example.chatup.data.model.ConversationList
import com.example.chatup.data.source.ConversationDataSource
import com.example.chatup.data.source.UserDataSource
import com.example.chatup.domain.repository.ConversationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val conversationDataSource: ConversationDataSource,
    private val userDataSource: UserDataSource
) : ConversationRepository {

    override fun observeConversations(): Flow<List<ConversationList>> = callbackFlow {
        val registration = conversationDataSource.observeConversations { docs, currentUserId ->
            launch {
                try {
                    val users = userDataSource.getAllUsersExceptCurrent()
                    val convList = docs.mapNotNull { doc ->
                        val conversationType = doc.getString("conversationType") ?: "private"
                        val usersInConversation = doc.get("users") as? List<String> ?: emptyList()
                        val groupName = doc.getString("name") ?: ""
                        val friendUsername = if (conversationType == "private") {
                            val friendId = usersInConversation.firstOrNull { it != currentUserId }
                            users.firstOrNull { it.uid == friendId }?.username ?: ""
                        } else {
                            groupName
                        }
                        ConversationList(
                            conversationId = doc.id,
                            lastMessage = doc.getString("lastMessage") ?: "",
                            lastUpdated = doc.getLong("lastUpdated") ?: 0L,
                            friendUsername = friendUsername,
                            users = usersInConversation,
                            lastMessageDelivered = doc.getBoolean("lastMessageDelivered") ?: false,
                            lastMessageSeen = doc.getBoolean("lastMessageSeen") ?: false,
                            conversationType = conversationType,
                            name = groupName
                        )
                    }
                    trySend(convList)
                } catch (e: Exception) {
                    Log.e("ConversationRepo", "Error building conversation list", e)
                }
            }
        }
        awaitClose { registration?.remove() }
    }
}
