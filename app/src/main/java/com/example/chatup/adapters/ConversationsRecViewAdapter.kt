package com.example.chatup.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.chatup.R
import com.example.chatup.data.ChatMessage
import com.example.chatup.databinding.ItemConversationListLayoutBinding
import com.example.chatup.data.ConversationList

class ConversationsRecViewAdapter :
    RecyclerView.Adapter<ConversationsRecViewAdapter.ConversationsViewHolder>() {


    private var conversationsList: List<ConversationList> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConversationsViewHolder {
        val binding = ItemConversationListLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationsViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ConversationsViewHolder,
        position: Int
    ) {
        val conversation = conversationsList[position]

        holder.binding.tvMessageIml.text = conversation.lastMessage
    }

    override fun getItemCount(): Int {
        return conversationsList.size
    }

    inner class ConversationsViewHolder(val binding: ItemConversationListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}