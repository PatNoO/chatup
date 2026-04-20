package com.example.chatup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatup.data.User
import com.example.chatup.data.source.UserDataSource
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ProfileViewModel : ViewModel() {

    // Temporary manual wiring — replaced by @HiltViewModel @Inject in CU-5
    private val userDataSource by lazy { UserDataSource(Firebase.auth, Firebase.firestore) }

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun loadUserProfile() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        userDataSource.loadProfile(uid) { user -> _currentUser.value = user }
    }

    fun updateUserProfile(username: String, profileImageUrl: String?) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        userDataSource.updateProfile(uid, username, profileImageUrl) {
            val updatedUser = _currentUser.value?.copy(
                username = username,
                profileImage = profileImageUrl ?: _currentUser.value?.profileImage
            )
            _currentUser.value = updatedUser
        }
    }
}
