package com.example.chatup.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.data.model.User
import com.example.chatup.domain.repository.AuthRepository
import com.example.chatup.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun loadUserProfile() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _currentUser.postValue(userRepository.loadProfile(uid))
        }
    }

    fun updateUserProfile(username: String, profileImageUrl: String?) {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            userRepository.updateProfile(uid, username, profileImageUrl)
            val updatedUser = _currentUser.value?.copy(
                username = username,
                profileImage = profileImageUrl ?: _currentUser.value?.profileImage
            )
            _currentUser.postValue(updatedUser)
        }
    }
}
