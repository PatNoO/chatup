package com.example.chatup.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatup.R
import com.example.chatup.ui.chat.ChatActivity
import com.example.chatup.ui.group.ChooseGroupMembersActivity
import com.example.chatup.ui.adapters.UserAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_users_list) {

    private val userViewModel: UsersViewModel by viewModels()
    private lateinit var adapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGroupChat = view.findViewById<View>(R.id.btn_group_chat)
        btnGroupChat.setOnClickListener {
            startActivity(Intent(requireContext(), ChooseGroupMembersActivity::class.java))
        }

        val recycler = view.findViewById<RecyclerView>(R.id.usersRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserAdapter(emptyList()) { user ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("userId", user.uid)
            intent.putExtra("userName", user.username)
            startActivity(intent)
        }
        recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.uiState.collect { state ->
                    adapter.update(state.users)
                }
            }
        }

        userViewModel.getAllUsers()
    }
}
