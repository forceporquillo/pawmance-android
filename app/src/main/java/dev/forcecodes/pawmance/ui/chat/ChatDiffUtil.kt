package dev.forcecodes.pawmance.ui.chat

import androidx.recyclerview.widget.DiffUtil
import dev.forcecodes.pawmance.model.ChatItems
import dev.forcecodes.pawmance.model.ConversationUiModel

internal val CONVERSATION_COMPARATOR = object : DiffUtil.ItemCallback<ConversationUiModel>() {
  override fun areItemsTheSame(
    oldItem: ConversationUiModel,
    newItem: ConversationUiModel
  ): Boolean {
    return oldItem.id == newItem.id
  }

  override fun areContentsTheSame(
    oldItem: ConversationUiModel,
    newItem: ConversationUiModel
  ): Boolean {
    return oldItem == newItem
  }
}

internal val CHAT_COMPARATOR = object : DiffUtil.ItemCallback<ChatItems>() {

  override fun areItemsTheSame(oldItem: ChatItems, newItem: ChatItems): Boolean {
    return oldItem.name == newItem.name
  }

  override fun areContentsTheSame(oldItem: ChatItems, newItem: ChatItems): Boolean {
    return oldItem == newItem
  }
}
