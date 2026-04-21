package com.example.chatup.ui.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.data.model.User
import com.example.chatup.domain.repository.GroupChatRepository
import com.example.chatup.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val groupChatRepository: GroupChatRepository
) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _createGroupResult = MutableLiveData<String?>()
    val createGroupResult: LiveData<String?> = _createGroupResult

    private var originalUserList = listOf<User>()

    fun getAllUsers() {
        viewModelScope.launch {
            try {
                val userList = userRepository.getUsers()
                originalUserList = userList
                _users.postValue(userList)
            } catch (e: Exception) {
                Log.e("UsersViewModel", "Error loading users: ${e.message}")
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _users.value = originalUserList
        } else {
            _users.value = originalUserList.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true
                        || user.email.contains(query, ignoreCase = true)
            }
        }
    }

    fun createGroup(groupName: String, members: List<String>) {
        viewModelScope.launch {
            try {
                val conversationId = groupChatRepository.createGroupConversation(groupName, members)
                _createGroupResult.postValue(conversationId)
            } catch (e: Exception) {
                Log.e("UsersViewModel", "Error creating group: ${e.message}")
            }
        }
    }
}
