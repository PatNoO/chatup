package com.example.chatup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatup.data.ChatMessage
import com.example.chatup.data.User
import com.example.chatup.data.source.ChatDataSource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

class ChatViewModel : ViewModel() {

    // Temporary manual wiring — replaced by @HiltViewModel @Inject in CU-5
    private val chatDataSource by lazy { ChatDataSource(Firebase.auth, Firebase.firestore) }

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

    fun setOtherUserName(otherUserName: String?) {
        _otherUserName.value = otherUserName ?: ""
    }

    fun setOtherUserId(userId: String) {
        _otherUserId.value = userId
    }

    fun setChatOpened(isOpened: Boolean) {
        _chatOpened.postValue(isOpened)
    }

    fun isChatOpened(): Boolean = _chatOpened.value == true

    fun setTyping(isTyping: Boolean) {
        chatDataSource.setTyping(conversationId, isTyping)
    }

    fun initChat(otherUserId: String) {
        _otherUserId.value = otherUserId
        conversationId = chatDataSource.createConversationId(otherUserId)

        chatListener?.remove()
        chatListener = chatDataSource.observeMessages(
            conversationId = conversationId,
            onUpdate = { messages -> _chatMessage.postValue(messages.toList()) },
            chatIsOpened = { isChatOpened() }
        )

        typingListener?.remove()
        typingListener = chatDataSource.typingSnapshotListener(
            conversationId = conversationId,
            friendId = otherUserId
        ) { typing -> _isTyping.value = typing }
    }

    fun sendMessage(chatText: String) {
        val receiverId = _otherUserId.value ?: return
        chatDataSource.sendMessage(chatText, receiverId)
    }

    fun checkDeliveredMessage() {
        checkDeliveredListener?.remove()
        checkDeliveredListener = chatDataSource.markPrivateChatDelivered()
    }

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
