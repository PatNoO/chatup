package com.example.chatup.ui.conversations

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatup.R
import com.example.chatup.ui.chat.ChatActivity
import com.example.chatup.ui.adapters.ConversationListAdapter
import com.example.chatup.ui.conversations.ConversationListViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ConversationListFragment : Fragment(R.layout.fragment_conversation_list) {

    private val conversationListViewModel: ConversationListViewModel by viewModels()
    private lateinit var adapter: ConversationListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.conversationListRecycler)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        // ============== Initilize adapter and click =========
        adapter = ConversationListAdapter(emptyList()) { conversation ->

            val intent = Intent(requireContext(), ChatActivity::class.java)

            intent.putExtra("conversationId", conversation.conversationId)
            intent.putExtra("isGroup", conversation.conversationType == "group")

            if (conversation.conversationType == "group") {
                intent.putExtra("groupName", conversation.name)
                intent.putStringArrayListExtra("chatPartnersId", ArrayList(conversation.users))

            } else {
                val currentUserId = Firebase.auth.currentUser?.uid
                val friendId = conversation.users.first { it != currentUserId }

                intent.putExtra("userId", friendId)
                intent.putExtra("userName", conversation.friendUsername ?: "Chat")
            }

            startActivity(intent)
        }
        recycler.adapter = adapter

        conversationListViewModel.conversationList.observe(viewLifecycleOwner) {
            adapter.update(it)
        }

        conversationListViewModel.getAllCurrentUserConversationLists()
    }

    // ============== Update conversationlist =============
    override fun onResume() {
        super.onResume()
        conversationListViewModel.getAllCurrentUserConversationLists()
    }
}