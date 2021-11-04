package dev.forcecodes.pawmance.ui.chat

import com.devforcecodes.pawmance.databinding.ReceiverMessageBinding
import dev.forcecodes.pawmance.model.ConversationUiModel
import dev.forcecodes.pawmance.utils.bindImageWith

class TheirMessageViewHolder(
  private val binding: ReceiverMessageBinding
) : ConversationViewHolder(binding.root) {

  override fun bindMessage(conversations: ConversationUiModel) {
    binding.theirMessageText.text = conversations.content
    bindImageWith(binding.profileAvatar, conversations.petPetProfile)
  }
}