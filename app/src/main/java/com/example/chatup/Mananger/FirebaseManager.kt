package com.example.chatup.Mananger

import android.util.Log
import com.example.chatup.data.ChatMessage
import com.example.chatup.data.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlin.collections.get

/**
 * Singleton object for managing all Firebase operations.
 * Handles sending/receiving messages, group chats, typing status,
 * marking messages delivered/seen, and user fetching.
 */
object FirebaseManager {

    /**
     * Firestore database instance.
     * Init lazy to ensure it is only created when first accessed.
     */
    private val db by lazy { Firebase.firestore }

    /**
     * Holds the currently authenticated Firebase user.
     */
    private lateinit var currentUser: FirebaseUser

    /**
     * Listens for private messages that need to be marked as delivered.
     *
     * @return ListenerRegistration? A Firestore listener for updates, null if user not authenticated.
     */
    fun markPrivateChatDelivered(): ListenerRegistration? {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return null

        return db.collection("conversation")
            .whereArrayContains("users", currentUserId)
            .addSnapshotListener { conversations, e ->
                if (e != null) {
                    Log.e("!!!", "Failed to listen to conversations: ${e.message}")
                    return@addSnapshotListener
                }

                conversations?.documents?.forEach { conversationDoc ->
                    val conversationId = conversationDoc.id

                    db.collection("conversation")
                        .document(conversationId)
                        .collection("messages")
                        .whereEqualTo("receiverId", currentUserId)
                        .whereEqualTo("delivered", false)
                        .get()
                        .addOnSuccessListener { messages ->

                            messages?.documents?.forEach { msg ->
                                msg.reference.update("delivered", true)
                                    .addOnSuccessListener {
                                        Log.d("!!!", "Delivered marked for message ${msg.id}")
                                    }
                                    .addOnFailureListener { e3 ->
                                        Log.e("!!!", "Failed to mark delivered: ${e3.message}")
                                    }
                                val lastMessageId = conversationDoc.getString("lastMessageId")

                                if (msg.id == lastMessageId) {
                                    conversationDoc.reference.update(
                                        mapOf(
                                            "lastMessageDelivered" to true,
                                            "lastUpdated" to System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                        }
                }
            }

    }


    /**
     * Updates typing status for the current user in a specific conversation.
     *
     * @param conversationId The ID of the conversation document.
     * @param isTyping True if the user is currently typing, false otherwise.
     */
    fun setTyping(conversationId: String, isTyping: Boolean) {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        db.collection("conversation")
            .document(conversationId)
            .update("typing.$currentUserId", isTyping)
    }

    /**
     * Sets up a snapshot listener for the typing status of a friend.
     *
     * @param conversationId The ID of the conversation.
     * @param friendId The UID of the friend whose typing status to listen to.
     * @param onTyping Callback invoked whenever the typing status changes.
     *                 Returns true if the friend is typing, false otherwise.
     * @return ListenerRegistration The Firestore listener registration.
     */
    fun typingSnapShotListener(
        conversationId: String,
        friendId: String,
        onTyping: (Boolean) -> Unit
    ): ListenerRegistration {

        return db.collection("conversation")
            .document(conversationId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    e.message?.let { Log.e("!!!", it) }
                    return@addSnapshotListener
                }
                val typing = snapshot?.get("typing") as? Map<*, *> ?: return@addSnapshotListener
                val isTyping = typing[friendId] as? Boolean ?: false

                onTyping(isTyping)

            }

    }


    /**
     * Fetches all users from Firestore 'users' collection excluding the current user.
     *
     * @param onComplete Callback invoked with a list of users on success.
     * @param onException Callback invoked with an Exception on failure.
     */
    fun getAllUsers(onComplete: (List<User>) -> Unit, onException: (Exception) -> Unit) {

        val currentUserId = Firebase.auth.currentUser?.uid


        db.collection("users")
            .get()
            .addOnSuccessListener { snapshots ->
                val userList = snapshots.documents.mapNotNull { doc ->
                    val user = doc.toObject(User::class.java)?.copy(uid = doc.id)
                    if (user?.uid != currentUserId) user else null
                }

                onComplete(userList)

            }.addOnFailureListener { e ->
                Log.e("FirebaseManager", "Failed to fetch users", e)
                onException(e)
            }
    }

    /**
     * Sets up a real-time listener for messages in a specific conversation.
     *
     * @param conversationId Firestore document ID of the conversation.
     * @param onUpdate Callback invoked whenever messages change. Returns a list of ChatMessage.
     * @param chatIsOpened Function returning true if the chat is currently open.
     * @return ListenerRegistration? Firestore listener registration, null if user not authenticated.
     */
    fun snapShotListener(
        conversationId: String,
        onUpdate: (List<ChatMessage>) -> Unit, chatIsOpened: () -> Boolean
    ): ListenerRegistration? {


        val currentUserId = Firebase.auth.currentUser?.uid ?: return null


        return db.collection("conversation")
            .document(conversationId)
            .collection("messages")
            .orderBy("timeStamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("!!!", e.message.toString())
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val chatMessages = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.apply {
                        id = doc.id
                    }
                }
                onUpdate(chatMessages)

                snapshot.documents.forEach { doc ->
                    val message = doc.toObject(ChatMessage::class.java) ?: return@forEach

                    if (message.receiverId == currentUserId
                        && message.delivered
                        && !message.seen &&
                        chatIsOpened()
                    ) {

                        doc.reference.update("seen", true)
                    }

                }

                val lastDoc = snapshot.documents.lastOrNull() ?: return@addSnapshotListener
                val lastMessage =
                    lastDoc.toObject(ChatMessage::class.java) ?: return@addSnapshotListener


                if (lastMessage.receiverId == currentUserId
                    && lastMessage.delivered && !lastMessage.seen
                    && chatIsOpened()
                ) {

                    lastDoc.reference.update("seen", true)

                    db.collection("conversation")
                        .document(conversationId)
                        .update(
                            mapOf(
                                "lastMessageSeen" to true,
                                "lastUpdated" to System.currentTimeMillis()
                            )
                        )
                }

            }


    }


    /**
     * Sends a chat message to a specific user.
     * Ensures the sender is authenticated.
     * Creates or updates the conversation metadata.
     * Stores the message inside the conversation's messages collection.
     *
     * @param chatText The message content that will be sent.
     * @param receiverId The unique user ID (uid) of the message receiver.
     *
     */
    fun sendChatMessage(chatText: String, receiverId: String) {

        currentUser = Firebase.auth.currentUser ?: return

        val conversationId = getConversationId(currentUser.uid, receiverId)

        val chatMessageRef = db.collection("conversation")
            .document(conversationId)
            .collection("messages")
            .document()

        val chatMessage = ChatMessage(
            id = chatMessageRef.id,
            senderId = currentUser.uid,
            receiverId = receiverId,
            messages = chatText,
            timeStamp = System.currentTimeMillis(),
            delivered = false,
            seen = false
        )

        chatMessageRef.set(chatMessage)
            .addOnSuccessListener {

                // Update or create conversation metadata
                db.collection("conversation")
                    .document(conversationId)
                    .set(
                        mapOf(
                            "users" to listOf(currentUser.uid, receiverId),
                            "lastMessage" to chatText,
                            "lastUpdated" to System.currentTimeMillis(),
                            "lastMessageId" to chatMessageRef.id,
                            "lastMessageDelivered" to false,
                            "lastMessageSeen" to false
                        ),
                        SetOptions.merge()
                    )

            }.addOnFailureListener {
                Log.d(
                    "CONV_UPDATE",
                    "Conversation updated: delivered=false, seen=false, sender=${currentUser.uid}"
                )
            }
    }

    /**
     * Sends a message to a group chat.
     *
     * @param conversationId Firestore conversation document ID.
     * @param chatText The message content.
     * @param members List of member UIDs in the group.
     */
    fun sendGroupMessage(conversationId: String, chatText: String, members: List<String>) {
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        val groupMessageRef = db.collection("conversation")
            .document(conversationId)
            .collection("messages")
            .document()

        val groupMessage = ChatMessage(
            id = groupMessageRef.id,
            senderId = currentUserId,
            messages = chatText,
            receiverId = null,
            timeStamp = System.currentTimeMillis(),
            delivered = false,
            seen = false
        )

        groupMessageRef.set(groupMessage)

        db.collection("conversation")
            .document(conversationId)
            .update(
                mapOf(
                    "lastMessage" to chatText,
                    "lastMessageId" to groupMessageRef.id,
                    "lastMessageSeen" to false,
                    "lastUpdated" to System.currentTimeMillis()
                )
            )

    }

    /**
     * Creates a unique and consistent conversation ID for a chat between two users.
     * The two user IDs are sorted alphabetically so the order is always the same, no matter which user sends or receives a message.
     * The sorted IDs are then joined into a single string using an underscore.
     *
     * @param user1Id the first user ID
     * @param user2Id the second user ID
     * @return String The unique conversation ID.
     */
    fun getConversationId(user1Id: String, user2Id: String): String {
        return listOf(user1Id, user2Id).sorted().joinToString("_")
    }

    /**
     * Generates a conversation ID for the current user and another user.
     *
     * @param user2Id The other user's UID.
     * @return String The unique conversation ID, or empty if user not authenticated.
     */
    fun createConversationId(user2Id: String): String {
        currentUser = Firebase.auth.currentUser ?: return ""
        val user1Id: String = currentUser.uid
        return listOf(user1Id, user2Id).sorted().joinToString("_")
    }


    /**
     * Creates a new group conversation in Firestore.
     *
     * @param groupName The name of the group.
     * @param members List of member UIDs.
     * @param onComplete Callback returning the created conversation ID.
     */
    fun createGroupConversation(
        groupName: String,
        members: List<String>,
        onComplete: (String) -> Unit
    ) {

        val currentUserId = Firebase.auth.currentUser?.uid ?: return


        val allMembers = (members + currentUserId).distinct()


        val groupConversation = mapOf(
            "conversationType" to "group",
            "name" to groupName,
            "users" to allMembers,
            "lastMessage" to "",
            "lastUpdated" to System.currentTimeMillis()
        )

        db.collection("conversation")
            .add(groupConversation)
            .addOnSuccessListener { doc ->

                onComplete(doc.id)
            }.addOnFailureListener { e ->
                Log.e("DEBUG_GROUP_MSG", "Failed to create group: ${e.message}")

            }


    }
}