package com.example.chatup.data.source

import android.util.Log
import com.example.chatup.data.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    fun getConversationId(user1Id: String, user2Id: String): String =
        listOf(user1Id, user2Id).sorted().joinToString("_")

    fun createConversationId(user2Id: String): String {
        val user1Id = auth.currentUser?.uid ?: return ""
        return getConversationId(user1Id, user2Id)
    }

    fun setTyping(conversationId: String, isTyping: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("conversation").document(conversationId)
            .update("typing.$uid", isTyping)
    }

    fun typingSnapshotListener(
        conversationId: String,
        friendId: String,
        onTyping: (Boolean) -> Unit
    ): ListenerRegistration {
        return db.collection("conversation").document(conversationId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ChatDataSource", e.message.toString())
                    return@addSnapshotListener
                }
                val typing = snapshot?.get("typing") as? Map<*, *> ?: return@addSnapshotListener
                onTyping(typing[friendId] as? Boolean ?: false)
            }
    }

    fun observeMessages(
        conversationId: String,
        onUpdate: (List<ChatMessage>) -> Unit,
        chatIsOpened: () -> Boolean
    ): ListenerRegistration? {
        val currentUserId = auth.currentUser?.uid ?: return null

        return db.collection("conversation").document(conversationId)
            .collection("messages").orderBy("timeStamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    Log.e("ChatDataSource", e?.message.toString())
                    return@addSnapshotListener
                }

                val messages = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.apply { id = doc.id }
                }
                onUpdate(messages)

                snapshot.documents.forEach { doc ->
                    val message = doc.toObject(ChatMessage::class.java) ?: return@forEach
                    if (message.receiverId == currentUserId
                        && message.delivered
                        && !message.seen
                        && chatIsOpened()
                    ) {
                        doc.reference.update("seen", true)
                    }
                }

                val lastDoc = snapshot.documents.lastOrNull() ?: return@addSnapshotListener
                val lastMessage = lastDoc.toObject(ChatMessage::class.java) ?: return@addSnapshotListener
                if (lastMessage.receiverId == currentUserId
                    && lastMessage.delivered
                    && !lastMessage.seen
                    && chatIsOpened()
                ) {
                    lastDoc.reference.update("seen", true)
                    db.collection("conversation").document(conversationId).update(
                        mapOf(
                            "lastMessageSeen" to true,
                            "lastUpdated" to System.currentTimeMillis()
                        )
                    )
                }
            }
    }

    fun sendMessage(chatText: String, receiverId: String) {
        val currentUser = auth.currentUser ?: return
        val conversationId = getConversationId(currentUser.uid, receiverId)
        val messageRef = db.collection("conversation")
            .document(conversationId)
            .collection("messages")
            .document()

        val message = ChatMessage(
            id = messageRef.id,
            senderId = currentUser.uid,
            receiverId = receiverId,
            messages = chatText,
            timeStamp = System.currentTimeMillis(),
            delivered = false,
            seen = false
        )

        messageRef.set(message).addOnSuccessListener {
            db.collection("conversation").document(conversationId).set(
                mapOf(
                    "users" to listOf(currentUser.uid, receiverId),
                    "lastMessage" to chatText,
                    "lastUpdated" to System.currentTimeMillis(),
                    "lastMessageId" to messageRef.id,
                    "lastMessageDelivered" to false,
                    "lastMessageSeen" to false
                ),
                SetOptions.merge()
            )
        }
    }

    fun observeMessagesFlow(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = db.collection("conversation").document(conversationId)
            .collection("messages").orderBy("timeStamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    fun observeTypingFlow(conversationId: String, friendId: String): Flow<Boolean> = callbackFlow {
        val registration = db.collection("conversation").document(conversationId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val typing = snapshot?.get("typing") as? Map<*, *>
                trySend(typing?.get(friendId) as? Boolean ?: false)
            }
        awaitClose { registration.remove() }
    }

    suspend fun sendMessageSuspend(chatText: String, receiverId: String) {
        val currentUser = auth.currentUser ?: return
        val conversationId = getConversationId(currentUser.uid, receiverId)
        val messageRef = db.collection("conversation")
            .document(conversationId)
            .collection("messages")
            .document()

        val message = ChatMessage(
            id = messageRef.id,
            senderId = currentUser.uid,
            receiverId = receiverId,
            messages = chatText,
            timeStamp = System.currentTimeMillis(),
            delivered = false,
            seen = false
        )

        messageRef.set(message).await()
        db.collection("conversation").document(conversationId).set(
            mapOf(
                "users" to listOf(currentUser.uid, receiverId),
                "lastMessage" to chatText,
                "lastUpdated" to System.currentTimeMillis(),
                "lastMessageId" to messageRef.id,
                "lastMessageDelivered" to false,
                "lastMessageSeen" to false
            ),
            SetOptions.merge()
        ).await()
    }

    fun markMessagesSeen(conversationId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("conversation").document(conversationId)
            .collection("messages")
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("delivered", true)
            .whereEqualTo("seen", false)
            .get()
            .addOnSuccessListener { messages ->
                messages.documents.forEach { doc -> doc.reference.update("seen", true) }
                db.collection("conversation").document(conversationId).update(
                    mapOf("lastMessageSeen" to true, "lastUpdated" to System.currentTimeMillis())
                )
            }
    }

    suspend fun markPrivateChatDeliveredSuspend() {
        val currentUserId = auth.currentUser?.uid ?: return
        val conversations = db.collection("conversation")
            .whereArrayContains("users", currentUserId)
            .get().await()

        conversations.documents.forEach { conversationDoc ->
            val undelivered = db.collection("conversation").document(conversationDoc.id)
                .collection("messages")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("delivered", false)
                .get().await()

            val lastMessageId = conversationDoc.getString("lastMessageId")
            undelivered.documents.forEach { msg ->
                msg.reference.update("delivered", true)
                if (msg.id == lastMessageId) {
                    conversationDoc.reference.update(
                        mapOf("lastMessageDelivered" to true, "lastUpdated" to System.currentTimeMillis())
                    )
                }
            }
        }
    }

    fun markPrivateChatDelivered(): ListenerRegistration? {
        val currentUserId = auth.currentUser?.uid ?: return null
        return db.collection("conversation")
            .whereArrayContains("users", currentUserId)
            .addSnapshotListener { conversations, e ->
                if (e != null) {
                    Log.e("ChatDataSource", "Failed to listen: ${e.message}")
                    return@addSnapshotListener
                }
                conversations?.documents?.forEach { conversationDoc ->
                    val conversationId = conversationDoc.id
                    db.collection("conversation").document(conversationId)
                        .collection("messages")
                        .whereEqualTo("receiverId", currentUserId)
                        .whereEqualTo("delivered", false)
                        .get()
                        .addOnSuccessListener { messages ->
                            messages?.documents?.forEach { msg ->
                                msg.reference.update("delivered", true)
                                val lastMessageId = conversationDoc.getString("lastMessageId")
                                if (msg.id == lastMessageId) {
                                    conversationDoc.reference.update(
                                        mapOf(
                                            "lastMessageDelivered" to true,
                                            "lastUpdated" to System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                        }
                }
            }
    }
}
