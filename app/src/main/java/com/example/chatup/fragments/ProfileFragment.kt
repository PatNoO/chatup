package com.example.chatup.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.chatup.R
import com.example.chatup.activities.MainActivity
import com.example.chatup.viewmodel.ProfileViewModel

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val ivProfileImage = view.findViewById<ImageView>(R.id.ivProfileImage)
        val etProfileImageUrl = view.findViewById<EditText>(R.id.etProfileImageUrl)
        val btnSave = view.findViewById<Button>(R.id.btnSaveProfile)
        val btnChats = view.findViewById<Button>(R.id.btnChats)
        val btnUsers = view.findViewById<Button>(R.id.btnUsers)

        profileViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                etUsername.setText(user.username)
                tvEmail.text = user.email
                if (!user.profileImage.isNullOrEmpty()) {
                    etProfileImageUrl.setText(user.profileImage)
                    Glide.with(this)
                        .load(user.profileImage)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .error(android.R.drawable.sym_def_app_icon)
                        .into(ivProfileImage)
                }
            }
        }

        btnSave.setOnClickListener {
            val newUsername = etUsername.text.toString()
            val newImageUrl = etProfileImageUrl.text.toString()

            if (newUsername.isNotBlank()) {
                profileViewModel.updateUserProfile(
                    newUsername,
                    if (newImageUrl.isNotBlank()) newImageUrl else null
                )
                Toast.makeText(
                    requireContext(),
                    getString(R.string.profile_updated), Toast.LENGTH_SHORT
                ).show()

                if (newImageUrl.isNotBlank()) {
                    Glide.with(this).load(newImageUrl).into(ivProfileImage)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.username_empty), Toast.LENGTH_SHORT
                ).show()
            }
        }

        profileViewModel.loadUserProfile()

        btnChats.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(ConversationListFragment())
        }

        btnUsers.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(UsersFragment())
        }
    }
}