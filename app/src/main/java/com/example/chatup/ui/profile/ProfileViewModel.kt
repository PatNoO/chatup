package com.example.chatup.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.data.model.User
import com.example.chatup.domain.repository.AuthRepository
import com.example.chatup.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val user: User) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun loadUserProfile() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val user = userRepository.loadProfile(uid)
            _uiState.value = if (user != null) UiState.Success(user)
                             else UiState.Error("Profile not found")
        }
    }

    fun updateUserProfile(username: String, profileImageUrl: String?) {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            userRepository.updateProfile(uid, username, profileImageUrl)
            val current = (_uiState.value as? UiState.Success)?.user
            if (current != null) {
                _uiState.value = UiState.Success(
                    current.copy(
                        username = username,
                        profileImage = profileImageUrl ?: current.profileImage
                    )
                )
            }
        }
    }
}
