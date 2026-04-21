package com.example.chatup

import com.example.chatup.data.model.ConversationList
import com.example.chatup.fakes.FakeConversationRepository
import com.example.chatup.ui.conversations.ConversationListViewModel
import com.example.chatup.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConversationListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeConversationRepository: FakeConversationRepository
    private lateinit var viewModel: ConversationListViewModel

    @Before
    fun setUp() {
        fakeConversationRepository = FakeConversationRepository()
        viewModel = ConversationListViewModel(fakeConversationRepository)
    }

    @Test
    fun `initial uiState is Loading`() {
        assertTrue(viewModel.uiState.value is ConversationListViewModel.UiState.Loading)
    }

    @Test
    fun `uiState is Success when repository emits non-empty list`() = runTest {
        val conversations = listOf(
            ConversationList(conversationId = "conv1", friendUsername = "Alice"),
            ConversationList(conversationId = "conv2", friendUsername = "Bob")
        )
        viewModel.getAllCurrentUserConversationLists()

        fakeConversationRepository.conversationsFlow.value = conversations

        val state = viewModel.uiState.value
        assertTrue(state is ConversationListViewModel.UiState.Success)
        assertEquals(conversations, (state as ConversationListViewModel.UiState.Success).conversations)
    }

    @Test
    fun `uiState is Empty when repository emits empty list`() = runTest {
        viewModel.getAllCurrentUserConversationLists()

        fakeConversationRepository.conversationsFlow.value = emptyList()

        assertTrue(viewModel.uiState.value is ConversationListViewModel.UiState.Empty)
    }

    @Test
    fun `uiState updates when repository emits new data`() = runTest {
        viewModel.getAllCurrentUserConversationLists()

        fakeConversationRepository.conversationsFlow.value = listOf(
            ConversationList(conversationId = "conv1")
        )
        assertTrue(viewModel.uiState.value is ConversationListViewModel.UiState.Success)

        fakeConversationRepository.conversationsFlow.value = emptyList()
        assertTrue(viewModel.uiState.value is ConversationListViewModel.UiState.Empty)
    }
}
