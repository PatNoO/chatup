package com.example.chatup.ui.conversations

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatup.R
import com.example.chatup.ui.chat.ChatActivity
import com.example.chatup.ui.adapters.ConversationListAdapter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConversationListFragment : Fragment(R.layout.fragment_conversation_list) {

    private val conversationListViewModel: ConversationListViewModel by viewModels()
    private lateinit var adapter: ConversationListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.conversationListRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                conversationListViewModel.uiState.collect { state ->
                    when (state) {
                        is ConversationListViewModel.UiState.Success -> adapter.update(state.conversations)
                        is ConversationListViewModel.UiState.Empty -> adapter.update(emptyList())
                        else -> {}
                    }
                }
            }
        }

        conversationListViewModel.getAllCurrentUserConversationLists()
    }

    override fun onResume() {
        super.onResume()
        conversationListViewModel.getAllCurrentUserConversationLists()
    }
}
