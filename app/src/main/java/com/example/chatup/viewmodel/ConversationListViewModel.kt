package com.example.chatup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.data.ConversationList
import com.example.chatup.data.source.ConversationDataSource
import com.example.chatup.data.source.UserDataSource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class ConversationListViewModel : ViewModel() {

    // Temporary manual wiring — replaced by @HiltViewModel @Inject in CU-5
    private val conversationDataSource by lazy { ConversationDataSource(Firebase.auth, Firebase.firestore) }
    private val userDataSource by lazy { UserDataSource(Firebase.auth, Firebase.firestore) }

    private var conversationListener: ListenerRegistration? = null

    private val _conversationList = MutableLiveData<List<ConversationList>>()
    val conversationList: LiveData<List<ConversationList>> = _conversationList

    fun getAllCurrentUserConversationLists() {
        conversationListener?.remove()
        conversationListener = conversationDataSource.observeConversations { docs, currentUserId ->
            viewModelScope.launch {
                val users = userDataSource.getAllUsersExceptCurrent()

                val convList = docs.mapNotNull { doc ->
                    val conversationType = doc.getString("conversationType") ?: "private"
                    val usersInConversation = doc.get("users") as? List<String> ?: emptyList()

                    val lastMessage = doc.getString("lastMessage") ?: ""
                    val lastMessageSeen = doc.getBoolean("lastMessageSeen") ?: false
                    val lastMessageDelivered = doc.getBoolean("lastMessageDelivered") ?: false
                    val lastUpdated = doc.getLong("lastUpdated") ?: 0L
                    val groupName = doc.getString("name") ?: ""

                    val friendUsername = if (conversationType == "private") {
                        val friendId = usersInConversation.firstOrNull { it != currentUserId }
                        users.firstOrNull { it.uid == friendId }?.username ?: ""
                    } else {
                        groupName
                    }

                    ConversationList(
                        conversationId = doc.id,
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

    override fun onCleared() {
        conversationListener?.remove()
        conversationListener = null
        super.onCleared()
    }
}
