package com.example.chatup.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatup.R
import com.example.chatup.ui.chat.ChatActivity
import com.example.chatup.ui.group.ChooseGroupMembersActivity
import com.example.chatup.ui.adapters.UserAdapter
import com.example.chatup.ui.search.UsersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_users_list) {

    private val userViewModel: UsersViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    // ============== Starts when fragment view creats ================
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ============== Start group chat ==============
        val btnGroupChat = view.findViewById<View>(R.id.btn_group_chat)

        btnGroupChat.setOnClickListener {
            val intent = Intent(requireContext(), ChooseGroupMembersActivity::class.java)
            startActivity(intent)
        }

        // ============= Initilize ViewModel, find RecyclerView and set layout manager ==============

        val recycler = view.findViewById<RecyclerView>(R.id.usersRecycler)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        // =============== Create adapter and handle user click ==============
        adapter = UserAdapter(emptyList()) { user ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("userId", user.uid)
            intent.putExtra("userName", user.username)
            startActivity(intent)
        }

        // ============== Attach adapter ro RecyclerView ===============
        recycler.adapter = adapter

        // ============== Observe users livedata, update the list and fetch all users ===============
        userViewModel.users.observe(viewLifecycleOwner) { adapter.update(it) }
        userViewModel.getAllUsers()
    }
}