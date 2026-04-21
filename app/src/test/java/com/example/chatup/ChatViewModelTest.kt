package com.example.chatup

import com.example.chatup.fakes.FakeChatRepository
import com.example.chatup.ui.chat.ChatViewModel
import com.example.chatup.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeChatRepository: FakeChatRepository
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setUp() {
        fakeChatRepository = FakeChatRepository()
        viewModel = ChatViewModel(fakeChatRepository)
    }

    @Test
    fun `sendMessage calls repository with correct text and receiver`() = runTest {
        viewModel.initChat("user456")

        viewModel.sendMessage("Hello there")

        assertEquals("Hello there", fakeChatRepository.lastSentMessage)
        assertEquals("user456", fakeChatRepository.lastSentReceiverId)
    }

    @Test
    fun `sendMessage does nothing when otherUserId is empty`() = runTest {
        viewModel.sendMessage("Hello")

        assertEquals(null, fakeChatRepository.lastSentMessage)
    }

    @Test
    fun `initChat sets conversationId and updates otherUserId in state`() = runTest {
        viewModel.initChat("user789")

        assertEquals("user789", viewModel.uiState.value.otherUserId)
    }

    @Test
    fun `messages in uiState update when repository emits`() = runTest {
        val testMessage = com.example.chatup.data.model.ChatMessage(
            id = "msg1",
            messages = "Hi",
            senderId = "user789"
        )
        viewModel.initChat("user789")

        fakeChatRepository.messages.value = listOf(testMessage)

        assertEquals(listOf(testMessage), viewModel.uiState.value.messages)
    }

    @Test
    fun `setOtherUserName updates state`() {
        viewModel.setOtherUserName("Alice")

        assertEquals("Alice", viewModel.uiState.value.otherUserName)
    }
}
