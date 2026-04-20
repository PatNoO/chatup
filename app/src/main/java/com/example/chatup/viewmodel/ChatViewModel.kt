package com.example.chatup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.data.ChatMessage
import com.example.chatup.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private var messagesJob: Job? = null
    private var typingJob: Job? = null

    private var conversationId: String = ""

    private val _chatOpened = MutableLiveData(false)

    private val _isTyping = MutableLiveData<Boolean>()
    val isTyping: LiveData<Boolean> get() = _isTyping

    private val _otherUserName = MutableLiveData<String>()
    val otherUserName: LiveData<String> get() = _otherUserName

    private val _otherUserId = MutableLiveData<String>()

    private val _chatMessage = MutableLiveData<List<ChatMessage>>()
    val chatMessage: LiveData<List<ChatMessage>> get() = _chatMessage

    fun setOtherUserName(otherUserName: String?) {
        _otherUserName.value = otherUserName ?: ""
    }

    fun setOtherUserId(userId: String) {
        _otherUserId.value = userId
    }

    fun setChatOpened(isOpened: Boolean) {
        _chatOpened.value = isOpened
        if (isOpened && conversationId.isNotEmpty()) {
            chatRepository.markMessagesSeen(conversationId)
        }
    }

    fun isChatOpened(): Boolean = _chatOpened.value == true

    fun setTyping(isTyping: Boolean) {
        if (conversationId.isNotEmpty()) {
            chatRepository.setTyping(conversationId, isTyping)
        }
    }

    fun initChat(otherUserId: String) {
        _otherUserId.value = otherUserId
        conversationId = chatRepository.createConversationId(otherUserId)

        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.observeMessages(conversationId).collect { messages ->
                _chatMessage.postValue(messages)
                if (isChatOpened()) {
                    chatRepository.markMessagesSeen(conversationId)
                }
            }
        }

        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            chatRepository.observeTyping(conversationId, otherUserId).collect { typing ->
                _isTyping.postValue(typing)
            }
        }
    }

    fun sendMessage(chatText: String) {
        val receiverId = _otherUserId.value ?: return
        viewModelScope.launch {
            chatRepository.sendMessage(chatText, receiverId)
        }
    }

    fun checkDeliveredMessage() {
        viewModelScope.launch {
            chatRepository.markPrivateChatDelivered()
        }
    }

    override fun onCleared() {
        messagesJob?.cancel()
        typingJob?.cancel()
        super.onCleared()
    }
}
