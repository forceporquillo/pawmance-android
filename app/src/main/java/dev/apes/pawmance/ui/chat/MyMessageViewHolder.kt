package dev.apes.pawmance.ui.chat

import com.devforcecodes.pawmance.databinding.MyMessageBinding
import dev.apes.pawmance.model.ConversationUiModel
import dev.apes.pawmance.model.ConversationUiModel.SenderUiModel
import timber.log.Timber

class MyMessageViewHolder(
  private val binding: MyMessageBinding
) : ConversationViewHolder(binding.root) {

  override fun bindMessage(conversations: ConversationUiModel) {
    //binding.date.text = conversations.timestamp
    binding.myMessageText.text = conversations.content
  }
}