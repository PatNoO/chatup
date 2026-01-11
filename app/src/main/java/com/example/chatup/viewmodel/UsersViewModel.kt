package com.example.chatup.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatup.Mananger.FirebaseManager
import com.example.chatup.data.User

class UsersViewModel : ViewModel() {

    // ============== Livedata ==============
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    // ============== Data ==================
    private var originalUserList = listOf<User>()

    // ============== Fetch users ===========
    fun getAllUsers() {
        FirebaseManager.getAllUsers({ userList ->
            originalUserList = userList
            _users.value = userList
        }, { e ->
            Log.e("!!!", e.message.toString())
        })
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _users.value = originalUserList
        } else {
            val filteredList = originalUserList.filter { user ->
                val usernameMatch = user.username?.contains(query, ignoreCase = true) == true
                val emailMatch = user.email.contains(query, ignoreCase = true)
                usernameMatch || emailMatch
            }
            _users.value = filteredList
        }
    }

    fun createGroup(
        groupName: String,
        members: List<String>,
        onComplete: (String) -> Unit
    ) {
        FirebaseManager.createGroupConversation(
            groupName = groupName,
            members = members,
            onComplete = onComplete
        )
    }
}