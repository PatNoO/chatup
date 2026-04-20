package com.example.chatup.ui.group

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chatup.ui.chat.ChatActivity
import com.example.chatup.data.model.User
import com.example.chatup.databinding.ActivityChooseGroupMembersBinding
import com.example.chatup.ui.search.UsersViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for selecting users to create a new group chat.
 * Users can choose multiple friends from a ListView.
 * After selection, a group can be created and ChatActivity is started.
 */
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

        loadUsers()
        initAdapter()
        selectUsersForGroupChat()

        usersViewModel.createGroupResult.observe(this) { conversationId ->
            if (conversationId != null) {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("conversationId", conversationId)
                intent.putExtra("isGroup", true)
                intent.putExtra("groupName", binding.etChooseUserAfl.text.toString().trim())
                startActivity(intent)
            }
        }

        binding.btnBackAfl.setOnClickListener { finish() }
    }

    /**
     * Initializes the ListView adapter for displaying friends with checkboxes.
     */
    private fun initAdapter() {
        adapter = ArrayAdapter(
            this,
            R.layout.simple_list_item_multiple_choice,
            friendList.map { it.username }
        )
        binding.lvUsersListAfl.adapter = adapter

    }

    /**
     * Sets up the logic to track which users are selected for the group chat.
     * Selected users are stored in list.
     */
    private fun selectUsersForGroupChat() {

        val selectedUser = mutableListOf<User>()

        binding.lvUsersListAfl.setOnItemClickListener { _, view, pos, _ ->

            val user = friendList[pos]
            val checkedView = view as CheckedTextView

            if (checkedView.isChecked) {
                selectedUser.add(user)
            } else {
                selectedUser.remove(user)
            }
        }

        startGroupChat(selectedUser)
    }

    /**
     * Starts a group chat with the selected users when the FAB is clicked.
     *
     * @param selectedUsers MutableList of User objects selected for the group.
     */
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

    /**
     * Loads all users/friends from Firestore via the UsersViewModel and updates the ListView.
     */
    fun loadUsers() {
        usersViewModel.getAllUsers()

        usersViewModel.users.observe(this) { userList ->
            friendList.clear()
            friendList.addAll(userList)
            adapter.clear()
            adapter.addAll(userList.map { it.username })
            adapter.notifyDataSetChanged()
        }
    }
}