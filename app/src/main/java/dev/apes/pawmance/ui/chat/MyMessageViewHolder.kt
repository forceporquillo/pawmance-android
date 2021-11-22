package dev.apes.pawmance.ui.chat

import com.devforcecodes.pawmance.databinding.MyMessageBinding
import dev.apes.pawmance.model.ConversationUiModel

class MyMessageViewHolder(
  private val binding: MyMessageBinding
) : ConversationViewHolder(binding.root) {

  override fun bindMessage(conversations: ConversationUiModel) {
    binding.myMessageText.text = conversations.content
  }
}