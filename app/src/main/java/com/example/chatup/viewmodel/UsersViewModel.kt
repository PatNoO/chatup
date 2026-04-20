package com.example.chatup.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatup.data.User
import com.example.chatup.data.source.GroupChatDataSource
import com.example.chatup.data.source.UserDataSource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class UsersViewModel : ViewModel() {

    // Temporary manual wiring — replaced by @HiltViewModel @Inject in CU-5
    private val userDataSource by lazy { UserDataSource(Firebase.auth, Firebase.firestore) }
    private val groupChatDataSource by lazy { GroupChatDataSource(Firebase.auth, Firebase.firestore) }

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private var originalUserList = listOf<User>()

    fun getAllUsers() {
        userDataSource.getAllUsers(
            onComplete = { userList ->
                originalUserList = userList
                _users.value = userList
            },
            onException = { e -> Log.e("UsersViewModel", e.message.toString()) }
        )
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

    fun createGroup(groupName: String, members: List<String>, onComplete: (String) -> Unit) {
        groupChatDataSource.createGroupConversation(groupName, members, onComplete)
    }
}
