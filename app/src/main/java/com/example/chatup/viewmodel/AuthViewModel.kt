package com.example.chatup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<Unit>>()
    val loginResult: LiveData<Result<Unit>> = _loginResult

    private val _registerResult = MutableLiveData<Result<Unit>>()
    val registerResult: LiveData<Result<Unit>> = _registerResult

    private val _googleLoginResult = MutableLiveData<Result<Unit>>()
    val googleLoginResult: LiveData<Result<Unit>> = _googleLoginResult

    private val _resetPasswordResult = MutableLiveData<Result<String>>()
    val resetPasswordResult: LiveData<Result<String>> = _resetPasswordResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = authRepository.login(email, password)
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _googleLoginResult.value = authRepository.loginWithGoogle(idToken)
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _registerResult.value = authRepository.register(email, password)
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _resetPasswordResult.value = Result.failure(Exception("Enter email address"))
            return
        }
        viewModelScope.launch {
            _resetPasswordResult.value = authRepository.sendPasswordReset(email)
        }
    }

    fun signOut() = authRepository.signOut()

    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()

    fun getCurrentUserEmail(): String? = authRepository.getCurrentUserEmail()
}
