package com.example.chatup.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.chatup.R
import com.example.chatup.StartMenuActivity
import com.example.chatup.databinding.ActivityLoginBinding
import com.example.chatup.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    // ============= Variables for binding and ViewModel =============
    lateinit var binding: ActivityLoginBinding
    lateinit var authViewModel: AuthViewModel

    // ============== Variables for credential manager ==============
    lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ============== View binding set up =============
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ============== Initilize ViewModel ==============
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        // ==============Initilize credential manager ==============
        credentialManager = CredentialManager.create(this)

        // ============== Clicklistener ==============
        binding.btnRegisterAl.setOnClickListener {
            if (checkValidInput()) {
                register()
            }
        }
        binding.btnLoginAl.setOnClickListener {
            if (checkValidInput()) {
                login()
            }
        }
        binding.btnLogingoogleAl.setOnClickListener {
            loginWithGoogle()
        }

        binding.btnForgotPasswordAl.setOnClickListener {
            val email = binding.etForgotEmailAl.text.toString().trim()
            authViewModel.resetPassword(email)
        }

        // ============== Observe reset password result =============
        authViewModel.resetPasswordResult.observe(this) { result ->
            result
                .onSuccess {
                    Toast.makeText(
                        this,
                        it,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onFailure {
                    Toast.makeText(
                        this,
                        it.message ?: getString(R.string.wrong),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    // ============== Google login ===============
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


                val result = credentialManager.getCredential(
                    this@LoginActivity,
                    request
                )

                handleSignIn(result)
            } catch (e: GetCredentialException) {
                handleFailure(e)
            }
        }
    }

    // ============== Handle successful Google login ===============
    private fun handleSignIn(result: GetCredentialResponse) {

        if (result.credential is CustomCredential && result.credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

            val idToken = googleIdTokenCredential.idToken

            authViewModel.loginWithGoogle(
                idToken,
                {
                    val intent = Intent(this, StartMenuActivity::class.java)
                    startActivity(intent)
                }, {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                })
        }
    }

    // =============== Handle login failure ==============
    private fun handleFailure(e: GetCredentialException) {
        when (e) {
            is GetCredentialCancellationException -> {
                Toast.makeText(this, getString(R.string.login_canceled), Toast.LENGTH_SHORT).show()
            }

            is NoCredentialException -> {
                Toast.makeText(this, getString(R.string.no_google_accounts), Toast.LENGTH_SHORT)
                    .show()
            }

            else -> {
                Toast.makeText(this, getString(R.string.error, e.message), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // ============== Clearing and validation functions ==============
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

    // ============== Email and password login ==============
    fun login() {
        val email = binding.etEmailAl.text.toString()
        val password = binding.etPasswordAl.text.toString()

        authViewModel.login(email, password, {
            clearFields()
            val intent = Intent(this, StartMenuActivity::class.java)
            startActivity(intent)
        }, {
            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
        })
    }

    // ============== Register with email and password ==============
    fun register() {
        val email = binding.etEmailAl.text.toString()
        val password = binding.etPasswordAl.text.toString()

        authViewModel.register(email, password) {
            if (it.isSuccessful) {
                clearFields()
                val intent = Intent(this, StartMenuActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()

            }
        }
    }
}