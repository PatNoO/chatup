package com.example.chatup.data.source

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun observeConversations(
        onUpdate: (docs: List<DocumentSnapshot>, currentUserId: String) -> Unit
    ): ListenerRegistration? {
        val currentUserId = auth.currentUser?.uid ?: return null

        return db.collection("conversation")
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val userDocs = snapshot.documents.filter { doc ->
                    val users = doc.get("users") as? List<String> ?: emptyList()
                    users.contains(currentUserId)
                }
                onUpdate(userDocs, currentUserId)
            }
    }
}
