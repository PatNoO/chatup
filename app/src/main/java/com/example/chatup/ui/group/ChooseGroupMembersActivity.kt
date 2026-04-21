package com.example.chatup.ui.group

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.chatup.ui.chat.ChatActivity
import com.example.chatup.data.model.User
import com.example.chatup.databinding.ActivityChooseGroupMembersBinding
import com.example.chatup.ui.search.UsersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChooseGroupMembersActivity : AppCompatActivity() {
    private val usersViewModel: UsersViewModel by viewModels()
    private lateinit var binding: ActivityChooseGroupMembersBinding
    private lateinit var adapter: ArrayAdapter<String>
    private var friendList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseGroupMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAdapter()
        loadUsers()
        selectUsersForGroupChat()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                usersViewModel.uiState.collect { state ->
                    if (state.users.isNotEmpty()) {
                        friendList.clear()
                        friendList.addAll(state.users)
                        adapter.clear()
                        adapter.addAll(state.users.map { it.username })
                        adapter.notifyDataSetChanged()
                    }
                    state.createGroupConversationId?.let { conversationId ->
                        usersViewModel.resetCreateGroupState()
                        val intent = Intent(this@ChooseGroupMembersActivity, ChatActivity::class.java)
                        intent.putExtra("conversationId", conversationId)
                        intent.putExtra("isGroup", true)
                        intent.putExtra("groupName", binding.etChooseUserAfl.text.toString().trim())
                        startActivity(intent)
                    }
                }
            }
        }

        binding.btnBackAfl.setOnClickListener { finish() }
    }

    private fun initAdapter() {
        adapter = ArrayAdapter(
            this,
            R.layout.simple_list_item_multiple_choice,
            friendList.map { it.username }
        )
        binding.lvUsersListAfl.adapter = adapter
    }

    private fun selectUsersForGroupChat() {
        val selectedUser = mutableListOf<User>()

        binding.lvUsersListAfl.setOnItemClickListener { _, view, pos, _ ->
            val user = friendList[pos]
            val checkedView = view as CheckedTextView
            if (checkedView.isChecked) selectedUser.add(user) else selectedUser.remove(user)
        }

        startGroupChat(selectedUser)
    }

    private fun startGroupChat(selectedUsers: MutableList<User>) {
        binding.fabStartGroupChatAfl.setOnClickListener {
            val groupName = binding.etChooseUserAfl.text.toString().trim()
            if (groupName.isBlank()) {
                Toast.makeText(this, getString(com.example.chatup.R.string.choose_a_group_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedUsers.size < 2) {
                Toast.makeText(this, getString(com.example.chatup.R.string.choose_2_users_for_group_chat), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            usersViewModel.createGroup(groupName, selectedUsers.map { it.uid })
        }
    }

    private fun loadUsers() {
        usersViewModel.getAllUsers()
    }
}
