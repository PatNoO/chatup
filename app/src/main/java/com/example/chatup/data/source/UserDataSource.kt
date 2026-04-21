package com.example.chatup.data.source

import android.util.Log
import com.example.chatup.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    fun getAllUsers(
        onComplete: (List<User>) -> Unit,
        onException: (Exception) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid
        db.collection("users").get()
            .addOnSuccessListener { snapshots ->
                val userList = snapshots.documents.mapNotNull { doc ->
                    val user = doc.toObject(User::class.java)?.copy(uid = doc.id)
                    if (user?.uid != currentUserId) user else null
                }
                onComplete(userList)
            }
            .addOnFailureListener { e ->
                Log.e("UserDataSource", "Failed to fetch users", e)
                onException(e)
            }
    }

    suspend fun getAllUsersExceptCurrent(): List<User> {
        val currentUserId = auth.currentUser?.uid
        val snapshot = db.collection("users").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val user = doc.toObject(User::class.java)?.copy(uid = doc.id)
            if (user?.uid != currentUserId) user else null
        }
    }

    fun loadProfile(uid: String, onSuccess: (User?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onSuccess(document.toObject(User::class.java)?.copy(uid = uid))
                } else {
                    onSuccess(null)
                }
            }
    }

    fun updateProfile(
        uid: String,
        username: String,
        profileImageUrl: String?,
        onSuccess: () -> Unit
    ) {
        val updates = hashMapOf<String, Any>("username" to username)
        if (profileImageUrl != null) updates["profileImage"] = profileImageUrl
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener { onSuccess() }
    }

    suspend fun loadProfileSuspend(uid: String): User? {
        val document = db.collection("users").document(uid).get().await()
        return if (document.exists()) document.toObject(User::class.java)?.copy(uid = uid) else null
    }

    suspend fun updateProfileSuspend(uid: String, username: String, profileImageUrl: String?) {
        val updates = hashMapOf<String, Any>("username" to username)
        if (profileImageUrl != null) updates["profileImage"] = profileImageUrl
        db.collection("users").document(uid).update(updates).await()
    }
}
