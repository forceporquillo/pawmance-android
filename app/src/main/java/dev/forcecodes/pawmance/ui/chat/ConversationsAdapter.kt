package dev.forcecodes.pawmance.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devforcecodes.pawmance.databinding.MyMessageBinding
import com.devforcecodes.pawmance.databinding.ReceiverMessageBinding
import dev.forcecodes.pawmance.model.ConversationUiModel

class ConversationsAdapter :
  ListAdapter<ConversationUiModel, ConversationViewHolder>(CONVERSATION_COMPARATOR) {

  private var recyclerView: RecyclerView? = null

  init {
    registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
      override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        recyclerView?.scrollToPosition(0)
      }
    })
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    this.recyclerView = recyclerView
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    super.onDetachedFromRecyclerView(recyclerView)
    this.recyclerView = null
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return if (viewType == RECEIVER) {
      MyMessageViewHolder(MyMessageBinding.inflate(inflater, parent, false))
    } else {
      TheirMessageViewHolder(ReceiverMessageBinding.inflate(inflater, parent, false))
    }
  }

  override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
    val conversations = getItem(position)
    holder.bindMessage(conversations)
  }

  override fun getItemViewType(position: Int): Int {
    return if (getItem(position).fromSender) SENDER else RECEIVER
  }

  companion object {
    private const val SENDER = 0
    private const val RECEIVER = 1
  }
}