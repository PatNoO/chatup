package com.example.chatup.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatup.R
import com.example.chatup.activities.ChatActivity
import com.example.chatup.adapters.UserAdapter
import com.example.chatup.viewmodel.UsersViewModel

class UsersFragment : Fragment(R.layout.fragment_users_list) {

    // ============== ViewModel and Adapter ==============
    private lateinit var userViewModel: UsersViewModel
    private lateinit var adapter: UserAdapter

    // ============== Starts when fragment view creats ================
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(this)[UsersViewModel::class.java]

        val recycler = view.findViewById<RecyclerView>(R.id.usersRecycler)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserAdapter(emptyList()) { user ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("userId", user.uid)
            intent.putExtra("userName", user.username)
            startActivity(intent)
        }
        recycler.adapter = adapter

        userViewModel.users.observe(viewLifecycleOwner) { adapter.update(it) }
        userViewModel.getAllUsers()
    }
}