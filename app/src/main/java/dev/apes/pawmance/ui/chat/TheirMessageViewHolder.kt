package dev.apes.pawmance.ui.chat

import com.devforcecodes.pawmance.databinding.ReceiverMessageBinding
import dev.apes.pawmance.model.ConversationUiModel
import dev.apes.pawmance.utils.bindImageWith

class TheirMessageViewHolder(
  private val binding: ReceiverMessageBinding
) : ConversationViewHolder(binding.root) {

  override fun bindMessage(conversations: ConversationUiModel) {
    binding.theirMessageText.text = conversations.content
    bindImageWith(binding.profileAvatar, conversations.petPetProfile)
  }
}