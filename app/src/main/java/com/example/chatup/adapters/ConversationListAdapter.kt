package com.example.chatup.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.chatup.R
import com.example.chatup.data.ConversationList
import com.example.chatup.databinding.ItemConversationListLayoutBinding

class ConversationListAdapter(
    private var conversationList: List<ConversationList>,
    private val onConversationClicked: (ConversationList) -> Unit
) :
    RecyclerView.Adapter<ConversationListAdapter.ConversationListViewHolder>() {

    // ============== ViewHolder reference for layouts ==============
    inner class ConversationListViewHolder(val binding: ItemConversationListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    // ============== Create ViewHolder and inflates layout ==============
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationListViewHolder {
        val binding = ItemConversationListLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationListViewHolder(binding)
    }

    // ============== Bind data for ViewHolder ==============
    override fun onBindViewHolder(holder: ConversationListViewHolder, position: Int) {
        val conversation = conversationList[position]
        holder.binding.tvMessageIml.text = conversation.lastMessage
        holder.binding.tvFriendName.text = conversation.friendUsername
        holder.binding.tvTimeStamp.text = TimeStamp(conversation.lastUpdated)

        if (conversation.conversationType == "group") {
            holder.binding.tvFriendName.text = conversation.name

            holder.binding.ivCheckSentIcl.isVisible = false
            holder.binding.ivCheckDeliveredIcl.isVisible = false
        } else {
            holder.binding.tvFriendName.text = conversation.friendUsername

            holder.binding.ivCheckSentIcl.isVisible = false
            holder.binding.ivCheckDeliveredIcl.isVisible = false


            when {
                conversation.lastMessageSeen -> {
                    holder.binding.ivCheckDeliveredIcl.setImageResource(R.drawable.seen_outline_check_small_24)
                    holder.binding.ivCheckSentIcl.setImageResource(R.drawable.seen_outline_check_small_24)
                    holder.binding.ivCheckSentIcl.isVisible = true
                    holder.binding.ivCheckDeliveredIcl.isVisible = true

                }

                conversation.lastMessageDelivered -> {
                    holder.binding.ivCheckDeliveredIcl.setImageResource(R.drawable.outline_check_small_24)
                    holder.binding.ivCheckSentIcl.setImageResource(R.drawable.outline_check_small_24)
                    holder.binding.ivCheckDeliveredIcl.isVisible = true
                    holder.binding.ivCheckSentIcl.isVisible = true
                }

                else -> {
                    holder.binding.ivCheckSentIcl.setImageResource(R.drawable.outline_check_small_24)
                    holder.binding.ivCheckSentIcl.isVisible = true
                }

            }


        }

        holder.binding.conversationListCardView.setOnClickListener {
            onConversationClicked(conversation)
        }
    }

    // ============== Function help for timestamp ==============
    private fun TimeStamp(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val dateFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val date = java.util.Date(timestamp)
        return dateFormat.format(date)
    }

    // ============== Returns number of objects in list ==============
    override fun getItemCount() = conversationList.size

    // ============== Updates adapter data and notify RecyclerView ==============
    fun update(newConvList: List<ConversationList>) {
        conversationList = newConvList
        notifyDataSetChanged()
    }
}