package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ChatMessage
import com.example.model.ChatSession
import com.example.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: AppRepository) : ViewModel() {

    private val currentUser = repository.currentUserFlow

    // Expose all active conversation list bubbles for chats list screen
    val chatSessions: StateFlow<List<ChatSession>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getActiveChatSessionsFlow(user.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current open chat messages flow
    private val _activeRecipientId = MutableStateFlow<String?>(null)
    val activeRecipientId = _activeRecipientId.asStateFlow()

    val activeMessages: StateFlow<List<ChatMessage>> = combine(
        currentUser,
        _activeRecipientId
    ) { user, recipientId ->
        user to recipientId
    }.flatMapLatest { (user, recipientId) ->
        if (user == null || recipientId == null) {
            flowOf(emptyList())
        } else {
            repository.getChatMessagesFlow(recipientId, user.id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setActiveChatRecipient(recipientId: String?) {
        _activeRecipientId.value = recipientId
    }

    fun sendMessage(text: String) {
        val recipientId = _activeRecipientId.value ?: return
        if (text.isBlank()) return
        
        viewModelScope.launch {
            repository.sendMessage(recipientId, text)
        }
    }
}
