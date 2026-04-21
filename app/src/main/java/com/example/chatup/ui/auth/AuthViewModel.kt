package com.example.chatup.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object LoginSuccess : UiState()
        object RegisterSuccess : UiState()
        object GoogleLoginSuccess : UiState()
        data class ResetPasswordSuccess(val email: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.login(email, password)
            _uiState.value = if (result.isSuccess) UiState.LoginSuccess
                             else UiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.loginWithGoogle(idToken)
            _uiState.value = if (result.isSuccess) UiState.GoogleLoginSuccess
                             else UiState.Error(result.exceptionOrNull()?.message ?: "Google login failed")
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.register(email, password)
            _uiState.value = if (result.isSuccess) UiState.RegisterSuccess
                             else UiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = UiState.Error("Enter email address")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val result = authRepository.sendPasswordReset(email)
            _uiState.value = if (result.isSuccess) UiState.ResetPasswordSuccess(result.getOrDefault(""))
                             else UiState.Error(result.exceptionOrNull()?.message ?: "Reset failed")
        }
    }

    fun resetState() { _uiState.value = UiState.Idle }

    fun signOut() = authRepository.signOut()

    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()

    fun getCurrentUserEmail(): String? = authRepository.getCurrentUserEmail()
}
