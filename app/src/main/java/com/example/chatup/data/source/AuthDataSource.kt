package com.example.chatup.data.source

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut() = auth.signOut()

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun loginWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun register(
        email: String,
        password: String,
        onResult: (Task<AuthResult>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    onResult(task)
                    return@addOnCompleteListener
                }
                val uid = task.result.user?.uid ?: return@addOnCompleteListener
                val user = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "username" to email.substringBefore("@")
                )
                db.collection("users").document(uid).set(user)
                    .addOnCompleteListener { onResult(task) }
            }
    }

    fun sendPasswordReset(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.setLanguageCode("sv")
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
            }
    }

    suspend fun loginSuspend(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    suspend fun loginWithGoogleSuspend(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        Unit
    }

    suspend fun registerSuspend(email: String, password: String): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("UID unavailable after registration")
        val user = hashMapOf(
            "uid" to uid,
            "email" to email,
            "username" to email.substringBefore("@")
        )
        db.collection("users").document(uid).set(user).await()
        Unit
    }

    suspend fun sendPasswordResetSuspend(email: String): Result<String> = runCatching {
        auth.setLanguageCode("sv")
        auth.sendPasswordResetEmail(email).await()
        email
    }
}
