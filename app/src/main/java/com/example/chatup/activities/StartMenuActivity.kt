package com.example.chatup

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.example.chatup.fragments.ConversationListFragment
import com.example.chatup.fragments.UsersFragment
import com.example.chatup.viewmodel.AuthViewModel
import com.example.chatup.viewmodel.ChatViewModel
import com.google.android.material.navigation.NavigationView
import android.widget.TextView
import androidx.core.view.GravityCompat
import com.example.chatup.activities.SettingsActivity
import com.example.chatup.activities.LoginActivity
import com.example.chatup.activities.ProfileActivity
import com.example.chatup.activities.SearchActivity
import com.example.chatup.activities.ChooseGroupMembersActivity
import com.example.chatup.databinding.StartMenuActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartMenuActivity : AppCompatActivity() {

    // ============== UI components ==============

    private lateinit var binding : StartMenuActivityBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    // ============== Viewmodels ==============
    private lateinit var auth: AuthViewModel
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StartMenuActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ============== ViewModels =============
        auth = ViewModelProvider(this)[AuthViewModel::class.java]
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // ============== Toolbar and navigation setup ==============
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // ============== Hamburger menu toggle ==============
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = Color.WHITE

        // ============== Set user email in navigation drawer header =============
        val headerViewHamburgerMenu = navigationView.getHeaderView(0)
        val tvMail = headerViewHamburgerMenu.findViewById<TextView>(R.id.tv_email)
        tvMail.text = auth.getCurrentUser()?.email ?: getString(R.string.no_email)

        // ============== Load default fragments ==============
        showConversations()
        showUsers()

        // ============== Handle navigation drawer menu click =============
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.menu_settings -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                R.id.menu_logout -> {
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }

                R.id.menu_chats -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    showConversations()
                    true
                }

                R.id.menu_users -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    showUsers()
                    true
                }

                R.id.menu_search -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(this, SearchActivity::class.java))
                    true
                }

                R.id.menu_profile -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                else -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }
    }

    // ============== Show fragments with conversations ==============
    private fun showConversations() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.conversationListContainer, ConversationListFragment())
            .commit()
        findViewById<FrameLayout>(R.id.conversationListContainer).visibility = View.VISIBLE
    }

    // ============= Show fragment with users ==============
    private fun showUsers() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.usersContainer, UsersFragment())
            .commit()
        findViewById<FrameLayout>(R.id.usersContainer).visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        chatViewModel.checkDeliveredMessage()
    }


}
