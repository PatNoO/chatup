package com.example.chatup.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.data.model.User
import com.example.chatup.domain.repository.GroupChatRepository
import com.example.chatup.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val groupChatRepository: GroupChatRepository
) : ViewModel() {

    data class UiState(
        val users: List<User> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val createGroupConversationId: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private var originalUserList = listOf<User>()

    fun getAllUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val userList = userRepository.getUsers()
                originalUserList = userList
                _uiState.value = _uiState.value.copy(users = userList, isLoading = false)
            } catch (e: Exception) {
                Log.e("UsersViewModel", "Error loading users: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun searchUsers(query: String) {
        val filtered = if (query.isBlank()) originalUserList
            else originalUserList.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true ||
                        user.email.contains(query, ignoreCase = true)
            }
        _uiState.value = _uiState.value.copy(users = filtered)
    }

    fun createGroup(groupName: String, members: List<String>) {
        viewModelScope.launch {
            try {
                val conversationId = groupChatRepository.createGroupConversation(groupName, members)
                _uiState.value = _uiState.value.copy(createGroupConversationId = conversationId)
            } catch (e: Exception) {
                Log.e("UsersViewModel", "Error creating group: ${e.message}")
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun resetCreateGroupState() {
        _uiState.value = _uiState.value.copy(createGroupConversationId = null)
    }
}
