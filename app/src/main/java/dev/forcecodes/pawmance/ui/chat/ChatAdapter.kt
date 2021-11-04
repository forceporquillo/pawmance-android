package dev.forcecodes.pawmance.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.devforcecodes.pawmance.databinding.ChatItemBinding
import dev.forcecodes.pawmance.model.ChatItems

class ChatAdapter : ListAdapter<ChatItems, ChatViewHolder>(CHAT_COMPARATOR) {

  var onConversationClick: (String) -> Unit = {}

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = ChatItemBinding.inflate(inflater, parent, false)
    return ChatViewHolder(binding, onConversationClick)
  }

  override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
    holder.bindChat(getItem(position))
  }
}