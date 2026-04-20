package com.example.chatup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatup.data.source.AuthDataSource
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class AuthViewModel : ViewModel() {

    // Temporary manual wiring — replaced by @HiltViewModel @Inject in CU-5
    private val authDataSource by lazy { AuthDataSource(Firebase.auth, Firebase.firestore) }

    private val _resetPasswordResult = MutableLiveData<Result<String>>()
    val resetPasswordResult: LiveData<Result<String>> = _resetPasswordResult

    fun register(email: String, password: String, callback: (Task<AuthResult>) -> Unit) {
        authDataSource.register(email, password, callback)
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        authDataSource.login(email, password, onSuccess, onFailure)
    }

    fun loginWithGoogle(idToken: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        authDataSource.loginWithGoogle(idToken, onSuccess, onFailure)
    }

    fun signOut() = authDataSource.signOut()

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _resetPasswordResult.value = Result.failure(Exception("Enter email address"))
            return
        }
        authDataSource.sendPasswordReset(email) { success, error ->
            if (success) _resetPasswordResult.postValue(Result.success("Reset email has been sent"))
            else _resetPasswordResult.postValue(Result.failure(Exception(error ?: "Error")))
        }
    }

    fun getCurrentUser() = authDataSource.getCurrentUser()
}
