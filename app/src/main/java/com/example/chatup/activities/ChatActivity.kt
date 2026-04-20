package com.example.chatup.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatup.R
import com.example.chatup.adapters.ChatRecViewAdapter
import com.example.chatup.databinding.ActivityChatBinding
import com.example.chatup.viewmodel.ChatViewModel
import com.example.chatup.viewmodel.GroupChatViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * ChatActivity handles both private and group chat conversations.
 * It initializes the UI, sets up ViewModels, and observes chat-related LiveData.
 */
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

        // Decide whether to start a group chat or a private chat
        if (isGroup) {
            val conversationId = intent.getStringExtra("conversationId")
            val chatPartnersIds = intent.getStringArrayListExtra("chatPartnersId") ?: emptyList()
            startGroupChat(conversationId, groupName, chatPartnersIds)
        } else {
            startPrivateChat(otherUserId, otherUserName)
        }

        binding.btnBackAc.setOnClickListener {
            finish()
        }
    }

    /**
     * Initializes a private chat conversation.
     * Sets up typing indicators, message observers, and send message handling.
     *
     * @param otherUserId The user ID of the chat partner.
     * @param otherUserName The display name of the chat partner.
     */
    private fun startPrivateChat(otherUserId: String?, otherUserName: String?) {
        if (otherUserId == null) {
            Log.e("DEBUG_GROUP", "conversationId is null!")
            return
        }
        chatViewModel.setOtherUserId(otherUserId)
        chatViewModel.initChat(otherUserId)
        chatViewModel.setOtherUserName(otherUserName)

        binding.tvReceiverNameAc.text = otherUserName

        binding.etMessageAc.addTextChangedListener { editText ->
            if (editText.isNullOrBlank()) {
                chatViewModel.setTyping(false)
            } else {
                chatViewModel.setTyping(true)
            }
        }

        chatViewModel.isTyping.observe(this) { isTyping ->
            if (isTyping) {
                binding.tvIsTextingAc.text = getString(R.string.is_typing, otherUserName)
            } else {
                binding.tvIsTextingAc.text = ""
            }
        }

        chatViewModel.otherUserName.observe(this) { name ->
            adapter.setChatUsers(isGroup = false, chatPartner = name)
        }

        chatViewModel.chatMessage.observe(this) { chatMessages ->
            adapter.submitList(chatMessages)
            if (chatMessages.isNotEmpty()) {
                binding.rvChatAc.scrollToPosition(chatMessages.size - 1) // scroll to last chatMessage
            }
        }


        binding.fabSendAc.setOnClickListener {
            val sendChatText = binding.etMessageAc.text.toString()
            if (sendChatText.isNotBlank()) {
                chatViewModel.sendMessage(sendChatText)
                binding.etMessageAc.text.clear()
            }
        }


    }


    /**
     * Initializes a group chat conversation.
     *
     * @param conversationId Unique ID of the group conversation.
     * @param groupName Name of the group chat.
     * @param chatPartnersId List of user IDs participating in the group chat.
     */
    fun startGroupChat(
        conversationId: String?,
        groupName: String?,
        chatPartnersId: List<String>
    ) {
        if (conversationId == null) {
            Log.e("DEBUG_GROUP", "conversationId is null!")
            return
        }

        binding.tvReceiverNameAc.text = groupName


        groupChatViewModel.initGroupChat(convId = conversationId, members = chatPartnersId)


        adapter.isGroupChat = true

        groupChatViewModel.usersMap.observe(this) { map ->
            adapter.updateUsersMap(map)
        }


        groupChatViewModel.groupChatMessage.observe(this) { messages ->
            adapter.submitList(messages.toList())
            if (messages.isNotEmpty()) {
                binding.rvChatAc.scrollToPosition(messages.size - 1)
            }
        }


        binding.fabSendAc.setOnClickListener {
            val sendChatText = binding.etMessageAc.text.toString()
            if (sendChatText.isNotBlank()) {
                groupChatViewModel.sendGroupMessage(sendChatText)
                binding.etMessageAc.text.clear()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        // Mark chat as opened when activity becomes visible
        chatViewModel.setChatOpened(true)
    }

    override fun onStop() {
        super.onStop()
        // Update chat state when activity is no longer visible
        if (intent.getBooleanExtra("isGroup", false)) {
            groupChatViewModel.setGroupChatOpened(false)
        } else {
            chatViewModel.setChatOpened(false)
            chatViewModel.setTyping(false)
        }
    }
}

