package com.example.chatup.ui.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatup.data.model.ConversationList
import com.example.chatup.domain.repository.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val conversations: List<ConversationList>) : UiState()
        object Empty : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private var observeJob: Job? = null

    fun getAllCurrentUserConversationLists() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            conversationRepository.observeConversations().collect { conversations ->
                _uiState.value = if (conversations.isEmpty()) UiState.Empty
                                 else UiState.Success(conversations)
            }
        }
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}
