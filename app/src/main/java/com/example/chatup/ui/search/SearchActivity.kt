package com.example.chatup.ui.search

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatup.R
import com.example.chatup.ui.adapters.UserAdapter
import com.example.chatup.ui.auth.AuthViewModel
import com.example.chatup.ui.auth.LoginActivity
import com.example.chatup.ui.auth.StartMenuActivity
import com.example.chatup.ui.chat.ChatActivity
import com.example.chatup.ui.profile.ProfileActivity
import com.example.chatup.ui.settings.SettingsActivity
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private val userViewModel: UsersViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var adapter: UserAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val recycler = findViewById<RecyclerView>(R.id.recyclerSearchResults)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = Color.WHITE

        val headerViewHamburgerMenu = navigationView.getHeaderView(0)
        val tvMail = headerViewHamburgerMenu.findViewById<TextView>(R.id.tv_email)
        tvMail.text = authViewModel.getCurrentUserEmail() ?: getString(R.string.no_email)

        setupDrawer()

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(emptyList()) { user ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("userId", user.uid)
            intent.putExtra("userName", user.username)
            startActivity(intent)
        }
        recycler.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.uiState.collect { state ->
                    adapter.update(state.users)
                }
            }
        }

        userViewModel.getAllUsers()

        etSearch.addTextChangedListener { text ->
            userViewModel.searchUsers(text.toString())
        }
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
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
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
