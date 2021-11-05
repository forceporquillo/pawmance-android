package dev.apes.pawmance.ui.chat

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import dev.apes.pawmance.model.ConversationUiModel

abstract class ConversationViewHolder(
  itemView: View
) : RecyclerView.ViewHolder(itemView) {

  abstract fun bindMessage(conversations: ConversationUiModel)
}



