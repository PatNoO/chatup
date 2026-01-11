package com.example.chatup.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class AuthRepository {

    // ============== Firebase auth ==============
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // ================ User registration ===============
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

                Firebase.firestore
                    .collection("users")
                    .document(uid)
                    .set(user)
                    .addOnCompleteListener {
                        onResult(task)
                    }
            }
    }

    // =============== Send password and reset ==============
    fun sendPasswordReset(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.setLanguageCode("sv")
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
}