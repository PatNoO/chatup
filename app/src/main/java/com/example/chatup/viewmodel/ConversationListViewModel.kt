package com.example.chatup.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatup.data.ConversationList
import com.example.chatup.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class ConversationListViewModel : ViewModel() {


    // ============== Variables for Firestore and Auth ==============
    private var conversationListener: ListenerRegistration? = null
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // ============== LiveData ===============
    private val _conversationList = MutableLiveData<List<ConversationList>>()
    val conversationList: LiveData<List<ConversationList>> = _conversationList

    // ============== Functions to get all users ==============
    fun getAllCurrentUserConversationLists() {
        val currentUserId = auth.currentUser?.uid ?: return

        conversationListener?.remove()

        conversationListener = db.collection("conversation")
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    return@addSnapshotListener
                }


                viewModelScope.launch {
                    val users = getUsers()

                    val convList = snapshot.documents.mapNotNull { doc ->
                        val conversationType = doc.getString("conversationType") ?: "private"
                        val usersInConversation = doc.get("users") as? List<String> ?: emptyList()
                        val conversationId = doc.id

                        if (!usersInConversation.contains(currentUserId)) return@mapNotNull null


                        // Default value from Firestore
                        val lastMessage = doc.getString("lastMessage") ?: ""
                        val lastMessageSeen = doc.getBoolean("lastMessageSeen") ?: false
                        val lastMessageDelivered = doc.getBoolean("lastMessageDelivered") ?: false
                        val lastUpdated = doc.getLong("lastUpdated") ?: 0L
                        val groupName = doc.getString("name") ?: ""
                        Log.d("DEBUG_CONV_LIST", "Conversation ${doc.id} has name='$groupName'")


                        val friendUsername = if (conversationType == "private") {
                            val friendId = usersInConversation.firstOrNull { it != currentUserId }
                            users.firstOrNull { it.uid == friendId }?.username ?: ""
                        } else {
                            groupName
                        }

                        ConversationList(
                            conversationId = conversationId,
                            lastMessage = lastMessage,
                            lastUpdated = lastUpdated,
                            friendUsername = friendUsername,
                            users = usersInConversation,
                            lastMessageDelivered = lastMessageDelivered,
                            lastMessageSeen = lastMessageSeen,
                            conversationType = conversationType,
                            name = groupName
                        )
                    }

                    _conversationList.postValue(convList)
                }
            }

    }

    // ============= Gets all users except the current one ==============
    private suspend fun getUsers(): List<User> {
        val currentUserId = auth.currentUser?.uid

        val usersSnapshot = db.collection("users").get().await()
        val users = usersSnapshot.documents.mapNotNull { doc ->
            val user = doc.toObject(User::class.java)?.copy(uid = doc.id)
            if (user?.uid != currentUserId) user else null
        }

        return users
    }


}