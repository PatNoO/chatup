package com.example.chatup.ui.profile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.chatup.R
import com.example.chatup.ui.auth.LoginActivity
import com.example.chatup.ui.auth.StartMenuActivity
import com.example.chatup.ui.search.SearchActivity
import com.example.chatup.ui.settings.SettingsActivity
import com.example.chatup.ui.auth.AuthViewModel
import com.example.chatup.ui.profile.ProfileViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlin.jvm.java
import kotlin.text.isNotBlank
import kotlin.text.isNullOrEmpty

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val toggle =
            ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = Color.WHITE

        val headerViewHamburgerMenu = navigationView.getHeaderView(0)
        val tvMail = headerViewHamburgerMenu.findViewById<TextView>(R.id.tv_email)
        tvMail.text = authViewModel.getCurrentUserEmail() ?: getString(R.string.no_email)

        setupDrawer()

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val ivProfileImage = findViewById<ImageView>(R.id.ivProfileImage)
        val etProfileImageUrl = findViewById<EditText>(R.id.etProfileImageUrl)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)

        profileViewModel.currentUser.observe(this) { user ->
            if (user != null) {
                etUsername.setText(user.username)
                tvEmail.text = user.email
                if (!user.profileImage.isNullOrEmpty()) {
                    etProfileImageUrl.setText(user.profileImage)
                    Glide.with(this)
                        .load(user.profileImage)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .error(android.R.drawable.sym_def_app_icon)
                        .into(ivProfileImage)
                }
            }
        }

        btnSave.setOnClickListener {
            val newUsername = etUsername.text.toString()
            val newImageUrl = etProfileImageUrl.text.toString()

            if (newUsername.isNotBlank()) {
                profileViewModel.updateUserProfile(
                    newUsername,
                    if (newImageUrl.isNotBlank()) newImageUrl else null
                )
                Toast.makeText(
                    this,
                    getString(R.string.profile_updated), Toast.LENGTH_SHORT
                ).show()

                if (newImageUrl.isNotBlank()) {
                    Glide.with(this).load(newImageUrl).into(ivProfileImage)
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.username_empty), Toast.LENGTH_SHORT
                ).show()
            }
        }

        profileViewModel.loadUserProfile()
    }

    private fun setupDrawer() {
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_chats -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, StartMenuActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.menu_users -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, StartMenuActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.menu_search -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }

                R.id.menu_settings -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                R.id.menu_logout -> {
                    authViewModel.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }

                R.id.menu_profile -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    // Redan på profilsidan
                    true
                }

                else -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }
    }
}
