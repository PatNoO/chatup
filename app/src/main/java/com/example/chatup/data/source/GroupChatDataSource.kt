package com.example.chatup.data.source

import android.util.Log
import com.example.chatup.data.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupChatDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    fun sendGroupMessage(conversationId: String, chatText: String, members: List<String>) {
        val currentUserId = auth.currentUser?.uid ?: return
        val messageRef = db.collection("conversation")
            .document(conversationId)
            .collection("messages")
            .document()

        val message = ChatMessage(
            id = messageRef.id,
            senderId = currentUserId,
            messages = chatText,
            receiverId = null,
            timeStamp = System.currentTimeMillis(),
            delivered = false,
            seen = false
        )

        messageRef.set(message)
        db.collection("conversation").document(conversationId).update(
            mapOf(
                "lastMessage" to chatText,
                "lastMessageId" to messageRef.id,
                "lastMessageSeen" to false,
                "lastUpdated" to System.currentTimeMillis()
            )
        )
    }

    fun createGroupConversation(
        groupName: String,
        members: List<String>,
        onComplete: (String) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: return
        val allMembers = (members + currentUserId).distinct()
        val groupConversation = mapOf(
            "conversationType" to "group",
            "name" to groupName,
            "users" to allMembers,
            "lastMessage" to "",
            "lastUpdated" to System.currentTimeMillis()
        )
        db.collection("conversation").add(groupConversation)
            .addOnSuccessListener { doc -> onComplete(doc.id) }
            .addOnFailureListener { e ->
                Log.e("GroupChatDataSource", "Failed to create group: ${e.message}")
            }
    }
}
