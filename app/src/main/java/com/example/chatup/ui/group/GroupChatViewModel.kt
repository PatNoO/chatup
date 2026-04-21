package com.example.chatup.ui.group

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.data.model.ChatMessage
import com.example.chatup.domain.repository.GroupChatRepository
import com.example.chatup.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val groupChatRepository: GroupChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    data class UiState(
        val messages: List<ChatMessage> = emptyList(),
        val usersMap: Map<String, String> = emptyMap()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private var messagesJob: Job? = null
    private lateinit var conversationId: String
    private lateinit var groupMembers: List<String>

    fun setGroupChatOpened(isOpened: Boolean) {}

    fun initGroupChat(convId: String?, members: List<String>) {
        if (convId == null) {
            Log.e("GroupChatViewModel", "initGroupChat: convId is null")
            return
        }
        conversationId = convId
        groupMembers = members

        loadUsersMap()

        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            groupChatRepository.observeGroupMessages(conversationId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    private fun loadUsersMap() {
        viewModelScope.launch {
            try {
                val users = userRepository.getUsers()
                _uiState.value = _uiState.value.copy(
                    usersMap = users.associate { it.uid to (it.username ?: "") }
                )
            } catch (e: Exception) {
                Log.e("GroupChatViewModel", "Error loading users: ${e.message}")
            }
        }
    }

    fun sendGroupMessage(chatText: String) {
        if (!::conversationId.isInitialized) {
            Log.e("GroupChatViewModel", "ViewModel not initialized")
            return
        }
        viewModelScope.launch {
            groupChatRepository.sendGroupMessage(conversationId, chatText, groupMembers)
        }
    }

    override fun onCleared() {
        messagesJob?.cancel()
        super.onCleared()
    }
}
