package dev.forcecodes.pawmance.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.pawmance.data.conversation.ConversationUseCase
import dev.forcecodes.pawmance.model.ChatItems
import dev.forcecodes.pawmance.utils.successOr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
  private val conversationUseCase: ConversationUseCase
) : ViewModel() {

  private val _recentConversations = MutableStateFlow<List<ChatItems>>(emptyList())
  val recentConversations = _recentConversations.asStateFlow()

  init {
    viewModelScope.launch {
      conversationUseCase(Any()).collect {
        _recentConversations.value = it.successOr(emptyList())
      }
    }
  }
}