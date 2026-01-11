package com.example.chatup.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatup.mananger.FirebaseManager
import com.example.chatup.data.ChatMessage
import com.google.firebase.firestore.ListenerRegistration

/**
 * ViewModel for handling group chat logic.
 * Responsible for initializing group chats, sending messages,
 * and observing chat messages and group member data.
 */
class GroupChatViewModel : ViewModel() {
    private var chatListener: ListenerRegistration? = null

    private lateinit var conversationId: String

    private lateinit var groupMembers: List<String>

    private val _chatIsOpened = MutableLiveData<Boolean>()

    private val _usersMap = MutableLiveData<Map<String, String>>(emptyMap())
    val usersMap: LiveData<Map<String, String>> get() = _usersMap


    private val _groupChatMessage = MutableLiveData<List<ChatMessage>>()
    val groupChatMessage: LiveData<List<ChatMessage>> get() = _groupChatMessage

    /**
     * Marks the group chat as opened or closed.
     *
     * @param isOpened True if the group chat is opened, false otherwise.
     * For now no use
     */
    fun setGroupChatOpened(isOpened: Boolean) {
        _chatIsOpened.value = isOpened
    }

    /**
     * Checks whether the group chat is currently opened.
     *
     * @return True if group chat is opened, false otherwise.
     * For now no use
     */
    private fun isGroupChatOpened(): Boolean {
        return _chatIsOpened.value == true
    }

    /**
     * Initializes a group chat with a conversation ID and members list.
     * Sets up message listener and loads users map.
     *
     * @param convId Unique ID of the group conversation.
     * @param members List of user IDs participating in the group chat.
     */
    fun initGroupChat(convId: String?, members: List<String>) {

        if (convId == null) {
            Log.e("DEBUG_GROUP", "initGroupChat: convId is null!")
            return
        }

        conversationId = convId
        groupMembers = members

        loadUsersMap()

        chatListener?.remove()

        chatListener = FirebaseManager.snapShotListener(
            conversationId = conversationId,
            onUpdate = { messages ->
                _groupChatMessage.postValue(messages)
            }, chatIsOpened = { isGroupChatOpened() }
        )

    }

    /**
     * Loads all users from Firebase and updates the users map.
     */
    private fun loadUsersMap() {
        FirebaseManager.getAllUsers(
            onComplete = { users ->
                val map: Map<String, String> = users.associate { user ->
                    Pair(user.uid, user.username.toString())
                }
                _usersMap.postValue(map)
            },
            onException = { e ->
                Log.e(e.message, "Error loading users")
            }
        )
    }

    /**
     * Sends a message to the group chat.
     *
     * @param chatText The text content of the message to send.
     */
    fun sendGroupMessage(chatText: String) {
        if (!::conversationId.isInitialized) {
            Log.e("GROUP_VM", "GroupChatViewModel not initialized")
            return
        }



        FirebaseManager.sendGroupMessage(
            conversationId = conversationId,
            chatText = chatText,
            members = groupMembers
        )

    }

    /**
     * Clears Firebase listeners when ViewModel is destroyed.
     */
    override fun onCleared() {
        chatListener?.remove()
        chatListener = null
        super.onCleared()
    }

}