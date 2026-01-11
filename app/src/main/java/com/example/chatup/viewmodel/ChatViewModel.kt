package com.example.chatup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatup.mananger.FirebaseManager
import com.example.chatup.data.ChatMessage
import com.example.chatup.data.User
import com.google.firebase.firestore.ListenerRegistration

/**
 * ViewModel for handling private chat logic.
 * Responsible for initializing chats, sending messages,
 * tracking typing status, and observing messages.
 */
class ChatViewModel : ViewModel() {

    private var checkDeliveredListener: ListenerRegistration? = null
    private var chatListener: ListenerRegistration? = null
    private var typingListener: ListenerRegistration? = null

    private var conversationId: String = ""

    private val _chatOpened = MutableLiveData<Boolean>()

    private val _isTyping = MutableLiveData<Boolean>()
    val isTyping: LiveData<Boolean> get() = _isTyping

    private val _otherUserName = MutableLiveData<String>()
    val otherUserName: LiveData<String> get() = _otherUserName

    private val _otherUserId = MutableLiveData<String>()

    private val _chatMessage = MutableLiveData<List<ChatMessage>>()
    val chatMessage: LiveData<List<ChatMessage>> get() = _chatMessage

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    /**
     * Sets the display name of the other user.
     *
     * @param otherUserName The name of the other user, can be null.
     */
    fun setOtherUserName(otherUserName: String?) {
        _otherUserName.value = otherUserName ?: ""
    }

    /**
     * Sets the user ID of the other user.
     *
     * @param userId The unique ID of the other user.
     */
    fun setOtherUserId(userId: String) {
        _otherUserId.value = userId
    }

    /**
     * Marks the chat as opened or closed.
     *
     * @param isOpened True if chat is opened, false if closed.
     */
    fun setChatOpened(isOpened: Boolean) {
        _chatOpened.postValue(isOpened)
    }

    /**
     * Checks if the chat is currently opened.
     *
     * @return True if chat is opened, false otherwise.
     */
    fun isChatOpened(): Boolean {
        return _chatOpened.value == true
    }

    /**
     * Updates the typing status in Firebase.
     *
     * @param isTyping True if the current user is typing, false otherwise.
     */
    fun setTyping(isTyping: Boolean) {
        FirebaseManager.setTyping(conversationId, isTyping)

    }

    /**
     * Initializes the chat with a specific user.
     * Sets up message and typing listeners.
     *
     * @param otherUserId The user ID of the chat partner.
     */
    fun initChat(otherUserId: String) {

        _otherUserId.value = otherUserId

        conversationId = FirebaseManager.createConversationId(otherUserId)

        chatListener?.remove()

        chatListener = FirebaseManager.snapShotListener(
            conversationId = conversationId,
            onUpdate = { messages ->
                _chatMessage.postValue(messages.toList())
            }, chatIsOpened = { isChatOpened() }
        )

        typingListener?.remove()

        typingListener = FirebaseManager.typingSnapShotListener(
            conversationId,
            otherUserId
        ) { typing ->
            _isTyping.value = typing
        }
    }

    /**
     * Sends a chat message to the other user.
     *
     * @param chatText The text content of the message to send.
     */
    fun sendMessage(chatText: String) {
        val receiverId = _otherUserId.value ?: return
        FirebaseManager.sendChatMessage(chatText, receiverId)
    }


    /**
     * Checks messages for delivery status (for the current user)
     */
    fun checkDeliveredMessage() {
        checkDeliveredListener?.remove()
        checkDeliveredListener = FirebaseManager.markPrivateChatDelivered()
    }

    /**
     * Clears listeners when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        chatListener?.remove()
        chatListener = null
        typingListener?.remove()
        typingListener = null
        checkDeliveredListener?.remove()
        checkDeliveredListener = null
    }

}