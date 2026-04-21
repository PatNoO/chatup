package com.example.chatup.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatup.R
import com.example.chatup.ui.adapters.ChatRecViewAdapter
import com.example.chatup.databinding.ActivityChatBinding
import com.example.chatup.ui.group.GroupChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private val groupChatViewModel: GroupChatViewModel by viewModels()
    private lateinit var adapter: ChatRecViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ChatRecViewAdapter()
        binding.rvChatAc.layoutManager = LinearLayoutManager(this)
        binding.rvChatAc.adapter = adapter

        val otherUserId = intent.getStringExtra("userId")
        val otherUserName = intent.getStringExtra("userName")
        val isGroup = intent.getBooleanExtra("isGroup", false)
        val groupName = intent.getStringExtra("groupName")

        if (isGroup) {
            val conversationId = intent.getStringExtra("conversationId")
            val chatPartnersIds = intent.getStringArrayListExtra("chatPartnersId") ?: emptyList()
            startGroupChat(conversationId, groupName, chatPartnersIds)
        } else {
            startPrivateChat(otherUserId, otherUserName)
        }

        binding.btnBackAc.setOnClickListener { finish() }
    }

    private fun startPrivateChat(otherUserId: String?, otherUserName: String?) {
        if (otherUserId == null) {
            Log.e("ChatActivity", "otherUserId is null")
            return
        }
        chatViewModel.setOtherUserId(otherUserId)
        chatViewModel.initChat(otherUserId)
        chatViewModel.setOtherUserName(otherUserName)

        binding.tvReceiverNameAc.text = otherUserName

        binding.etMessageAc.addTextChangedListener { editText ->
            chatViewModel.setTyping(!editText.isNullOrBlank())
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.uiState.collect { state ->
                    binding.tvIsTextingAc.text = if (state.isTyping)
                        getString(R.string.is_typing, otherUserName) else ""

                    adapter.setChatUsers(isGroup = false, chatPartner = state.otherUserName)

                    adapter.submitList(state.messages)
                    if (state.messages.isNotEmpty()) {
                        binding.rvChatAc.scrollToPosition(state.messages.size - 1)
                    }
                }
            }
        }

        binding.fabSendAc.setOnClickListener {
            val text = binding.etMessageAc.text.toString()
            if (text.isNotBlank()) {
                chatViewModel.sendMessage(text)
                binding.etMessageAc.text.clear()
            }
        }
    }

    private fun startGroupChat(
        conversationId: String?,
        groupName: String?,
        chatPartnersId: List<String>
    ) {
        if (conversationId == null) {
            Log.e("ChatActivity", "conversationId is null")
            return
        }

        binding.tvReceiverNameAc.text = groupName
        groupChatViewModel.initGroupChat(convId = conversationId, members = chatPartnersId)
        adapter.isGroupChat = true

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                groupChatViewModel.uiState.collect { state ->
                    adapter.updateUsersMap(state.usersMap)
                    adapter.submitList(state.messages.toList())
                    if (state.messages.isNotEmpty()) {
                        binding.rvChatAc.scrollToPosition(state.messages.size - 1)
                    }
                }
            }
        }

        binding.fabSendAc.setOnClickListener {
            val text = binding.etMessageAc.text.toString()
            if (text.isNotBlank()) {
                groupChatViewModel.sendGroupMessage(text)
                binding.etMessageAc.text.clear()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        chatViewModel.setChatOpened(true)
    }

    override fun onStop() {
        super.onStop()
        if (intent.getBooleanExtra("isGroup", false)) {
            groupChatViewModel.setGroupChatOpened(false)
        } else {
            chatViewModel.setChatOpened(false)
            chatViewModel.setTyping(false)
        }
    }
}
