package com.example.chatup.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.chatup.R
import com.example.chatup.databinding.ActivityLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialManager = CredentialManager.create(this)

        binding.btnRegisterAl.setOnClickListener { if (checkValidInput()) register() }
        binding.btnLoginAl.setOnClickListener { if (checkValidInput()) login() }
        binding.btnLogingoogleAl.setOnClickListener { loginWithGoogle() }
        binding.btnForgotPasswordAl.setOnClickListener {
            authViewModel.resetPassword(binding.etForgotEmailAl.text.toString().trim())
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.collect { state ->
                    when (state) {
                        is AuthViewModel.UiState.LoginSuccess,
                        is AuthViewModel.UiState.RegisterSuccess,
                        is AuthViewModel.UiState.GoogleLoginSuccess -> {
                            clearFields()
                            authViewModel.resetState()
                            startActivity(Intent(this@LoginActivity, StartMenuActivity::class.java))
                        }
                        is AuthViewModel.UiState.ResetPasswordSuccess -> {
                            Toast.makeText(this@LoginActivity, state.email, Toast.LENGTH_SHORT).show()
                            authViewModel.resetState()
                        }
                        is AuthViewModel.UiState.Error -> {
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                            authViewModel.resetState()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun loginWithGoogle() {
        lifecycleScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(baseContext.getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(this@LoginActivity, request)
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                handleFailure(e)
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        if (result.credential is CustomCredential && result.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            authViewModel.loginWithGoogle(googleIdTokenCredential.idToken)
        }
    }

    private fun handleFailure(e: GetCredentialException) {
        when (e) {
            is GetCredentialCancellationException ->
                Toast.makeText(this, getString(R.string.login_canceled), Toast.LENGTH_SHORT).show()
            is NoCredentialException ->
                Toast.makeText(this, getString(R.string.no_google_accounts), Toast.LENGTH_SHORT).show()
            else ->
                Toast.makeText(this, getString(R.string.error, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    fun clearFields() {
        binding.etPasswordAl.text.clear()
        binding.etEmailAl.text.clear()
        binding.etForgotEmailAl.text.clear()
    }

    fun checkValidInput(): Boolean {
        var check = true
        if (binding.etPasswordAl.text.isBlank()) {
            check = false
            Toast.makeText(this, getString(R.string.etPasswordAlBlank), Toast.LENGTH_SHORT).show()
        }
        if (binding.etEmailAl.text.isBlank()) {
            check = false
            Toast.makeText(this, getString(R.string.etEmailAlBlank), Toast.LENGTH_SHORT).show()
        }
        if (binding.etPasswordAl.text.length < 6) {
            check = false
            Toast.makeText(this, getString(R.string.etPasswordAlToShort), Toast.LENGTH_SHORT).show()
        }
        return check
    }

    private fun login() {
        authViewModel.login(
            binding.etEmailAl.text.toString(),
            binding.etPasswordAl.text.toString()
        )
    }

    private fun register() {
        authViewModel.register(
            binding.etEmailAl.text.toString(),
            binding.etPasswordAl.text.toString()
        )
    }
}
