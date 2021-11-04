package dev.forcecodes.pawmance.ui.chat

import androidx.recyclerview.widget.RecyclerView
import com.devforcecodes.pawmance.databinding.ChatItemBinding
import dev.forcecodes.pawmance.model.ChatItems
import dev.forcecodes.pawmance.utils.bindImageWith

class ChatViewHolder(
  private val binding: ChatItemBinding,
  onConversationClick: (String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

  private var chatId: String? = null

  init {
    binding.root.setOnClickListener {
      onConversationClick(chatId ?: return@setOnClickListener)
    }
  }

  fun bindChat(chatItems: ChatItems) {
    chatId = chatItems.recipientId
    bindImageWith(binding.profileAvatar, chatItems.profile)
    binding.userName.text = chatItems.name
    binding.recentMessage.text = chatItems.recentMessage
  }
}