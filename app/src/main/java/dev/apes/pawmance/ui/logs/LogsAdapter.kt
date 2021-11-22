package dev.apes.pawmance.ui.logs

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devforcecodes.pawmance.databinding.LogItemBinding
import dev.apes.pawmance.data.logs.Logs
import dev.apes.pawmance.ui.logs.LogsAdapter.LogsViewHolder
import dev.apes.pawmance.utils.TimeUtils

class LogsAdapter :
  ListAdapter<Logs, LogsViewHolder>(object : DiffUtil.ItemCallback<Logs>() {
    override fun areItemsTheSame(oldItem: Logs, newItem: Logs): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Logs, newItem: Logs): Boolean {
      return oldItem == newItem
    }
  }) {

  private val handler = Handler(Looper.getMainLooper())

  private lateinit var logsListView: RecyclerView

  init {
    registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
      override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        if (itemCount == 0) {
          return
        }
        scrollTo()
      }
    })
  }

  private fun scrollTo() {
    logsListView.scrollToPosition(itemCount - 1)
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    this.logsListView = recyclerView
  }

  class LogsViewHolder(
    private val binding: LogItemBinding
  ) : RecyclerView.ViewHolder(binding.root) {

    fun bindLogs(logs: Logs) {
      val date = formatDate(logs.timestamp)

      val stackTrace = "$date [${logs.tag}] ${logs.stack}"
      binding.log.text = stackTrace
    }

    private fun formatDate(timestamp: Long): String? {
      return TimeUtils.convertDate(timestamp)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewHolder {
    return LogsViewHolder(
      LogItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false
      )
    )
  }

  override fun onBindViewHolder(holder: LogsViewHolder, position: Int) {
    holder.bindLogs(getItem(position))
  }
}