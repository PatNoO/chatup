package com.example.chatup.data.model

data class ConversationList(
    val conversationId: String = "",
    val lastMessage: String = "",
    val lastUpdated: Long = 0,
    var friendUsername: String = "",
    val users: List<String> = emptyList(),
    var lastMessageDelivered: Boolean = false,
    var lastMessageSeen: Boolean = false,
    val conversationType: String = "private",
    val name: String = ""
)