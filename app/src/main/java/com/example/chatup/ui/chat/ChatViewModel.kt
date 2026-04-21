package com.example.chatup.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.data.model.ChatMessage
import com.example.chatup.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    data class UiState(
        val messages: List<ChatMessage> = emptyList(),
        val isTyping: Boolean = false,
        val otherUserName: String = "",
        val otherUserId: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private var messagesJob: Job? = null
    private var typingJob: Job? = null
    private var conversationId: String = ""
    private var chatOpened = false

    fun setOtherUserName(name: String?) {
        _uiState.value = _uiState.value.copy(otherUserName = name ?: "")
    }

    fun setOtherUserId(userId: String) {
        _uiState.value = _uiState.value.copy(otherUserId = userId)
    }

    fun setChatOpened(isOpened: Boolean) {
        chatOpened = isOpened
        if (isOpened && conversationId.isNotEmpty()) {
            chatRepository.markMessagesSeen(conversationId)
        }
    }

    fun isChatOpened(): Boolean = chatOpened

    fun setTyping(isTyping: Boolean) {
        if (conversationId.isNotEmpty()) {
            chatRepository.setTyping(conversationId, isTyping)
        }
    }

    fun initChat(otherUserId: String) {
        _uiState.value = _uiState.value.copy(otherUserId = otherUserId)
        conversationId = chatRepository.createConversationId(otherUserId)

        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.observeMessages(conversationId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
                if (chatOpened) chatRepository.markMessagesSeen(conversationId)
            }
        }

        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            chatRepository.observeTyping(conversationId, otherUserId).collect { typing ->
                _uiState.value = _uiState.value.copy(isTyping = typing)
            }
        }
    }

    fun sendMessage(chatText: String) {
        val receiverId = _uiState.value.otherUserId.ifEmpty { return }
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
