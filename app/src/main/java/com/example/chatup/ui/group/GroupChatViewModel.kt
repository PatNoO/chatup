package com.example.chatup.ui.group

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.data.model.ChatMessage
import com.example.chatup.domain.repository.GroupChatRepository
import com.example.chatup.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val groupChatRepository: GroupChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var messagesJob: Job? = null

    private lateinit var conversationId: String
    private lateinit var groupMembers: List<String>

    private val _chatIsOpened = MutableLiveData(false)

    private val _usersMap = MutableLiveData<Map<String, String>>(emptyMap())
    val usersMap: LiveData<Map<String, String>> get() = _usersMap

    private val _groupChatMessage = MutableLiveData<List<ChatMessage>>()
    val groupChatMessage: LiveData<List<ChatMessage>> get() = _groupChatMessage

    fun setGroupChatOpened(isOpened: Boolean) {
        _chatIsOpened.value = isOpened
    }

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
                _groupChatMessage.postValue(messages)
            }
        }
    }

    private fun loadUsersMap() {
        viewModelScope.launch {
            try {
                val users = userRepository.getUsers()
                _usersMap.postValue(users.associate { it.uid to (it.username ?: "") })
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
