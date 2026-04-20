package com.example.chatup.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatup.data.ChatMessage
import com.example.chatup.data.source.ChatDataSource
import com.example.chatup.data.source.GroupChatDataSource
import com.example.chatup.data.source.UserDataSource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

class GroupChatViewModel : ViewModel() {

    // Temporary manual wiring — replaced by @HiltViewModel @Inject in CU-5
    private val chatDataSource by lazy { ChatDataSource(Firebase.auth, Firebase.firestore) }
    private val groupChatDataSource by lazy { GroupChatDataSource(Firebase.auth, Firebase.firestore) }
    private val userDataSource by lazy { UserDataSource(Firebase.auth, Firebase.firestore) }

    private var chatListener: ListenerRegistration? = null

    private lateinit var conversationId: String
    private lateinit var groupMembers: List<String>

    private val _chatIsOpened = MutableLiveData<Boolean>()

    private val _usersMap = MutableLiveData<Map<String, String>>(emptyMap())
    val usersMap: LiveData<Map<String, String>> get() = _usersMap

    private val _groupChatMessage = MutableLiveData<List<ChatMessage>>()
    val groupChatMessage: LiveData<List<ChatMessage>> get() = _groupChatMessage

    fun setGroupChatOpened(isOpened: Boolean) {
        _chatIsOpened.value = isOpened
    }

    private fun isGroupChatOpened(): Boolean = _chatIsOpened.value == true

    fun initGroupChat(convId: String?, members: List<String>) {
        if (convId == null) {
            Log.e("GroupChatViewModel", "initGroupChat: convId is null")
            return
        }
        conversationId = convId
        groupMembers = members

        loadUsersMap()

        chatListener?.remove()
        chatListener = chatDataSource.observeMessages(
            conversationId = conversationId,
            onUpdate = { messages -> _groupChatMessage.postValue(messages) },
            chatIsOpened = { isGroupChatOpened() }
        )
    }

    private fun loadUsersMap() {
        userDataSource.getAllUsers(
            onComplete = { users ->
                _usersMap.postValue(users.associate { it.uid to (it.username ?: "") })
            },
            onException = { e -> Log.e("GroupChatViewModel", "Error loading users: ${e.message}") }
        )
    }

    fun sendGroupMessage(chatText: String) {
        if (!::conversationId.isInitialized) {
            Log.e("GroupChatViewModel", "ViewModel not initialized")
            return
        }
        groupChatDataSource.sendGroupMessage(conversationId, chatText, groupMembers)
    }

    override fun onCleared() {
        chatListener?.remove()
        chatListener = null
        super.onCleared()
    }
}
