package com.example.chatup.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatup.data.model.ConversationList
import com.example.chatup.data.model.User
import com.example.chatup.databinding.ItemUserBinding


class UserAdapter(private var users: List<User>, private val onUserClicked: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UsersViewHolder>() {

    // ============== ViewHolder class ================
    inner class UsersViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    // ============= Create new object of ViewHolder ==============
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsersViewHolder(binding)
    }

    // ============== Binding data to ViewHolder ==============
    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = users[position]
        holder.binding.tvUsername.text = user.username

        holder.binding.userCardView.setOnClickListener {
            onUserClicked(user)
        }
    }

    override fun getItemCount() = users.size

    // ============ Update and refresh list with users ==============
    fun update(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
