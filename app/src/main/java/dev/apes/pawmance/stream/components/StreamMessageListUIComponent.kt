package dev.apes.pawmance.stream.components

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.devforcecodes.pawmance.R
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory

class StreamMessageListUIComponent constructor(
    override val lifecycleOwner: LifecycleOwner,
    val cid: String,
    val messageId: String? = null
) : StreamUIComponent {

    private val factory by lazy(LazyThreadSafetyMode.NONE) {
        MessageListViewModelFactory(cid = cid, messageId = messageId)
    }
    private val messageListHeaderViewModel: MessageListHeaderViewModel by lazy(LazyThreadSafetyMode.NONE) {
        factory.create(MessageListHeaderViewModel::class.java)
    }
    val messageListViewModel: MessageListViewModel by lazy(LazyThreadSafetyMode.NONE) {
        factory.create(MessageListViewModel::class.java)
    }
    private val messageInputViewModel: MessageInputViewModel by lazy(LazyThreadSafetyMode.NONE) {
        factory.create(MessageInputViewModel::class.java)
    }

    @StreamComponents
    override fun bindLayout(view: View) {
        // binds Stream UI components to the ViewModels.
        val messageListHeaderView =
            view.findViewById<MessageListHeaderView>(R.id.messageListHeaderView)
        messageListHeaderView?.let {
            messageListHeaderViewModel.bindView(it, lifecycleOwner)
        }
        val messageListView =
            view.findViewById<MessageListView>(R.id.messageListView)
        messageListView?.let {
            messageListViewModel.bindView(it, lifecycleOwner)
        }
        val messageInputView =
            view.findViewById<MessageInputView>(R.id.messageInputView)
        messageInputView?.let {
            messageInputViewModel.bindView(it, lifecycleOwner)
        }

        // observe thread modes states.
        messageListViewModel.mode.observe(lifecycleOwner) {
            when (it) {
                is MessageListViewModel.Mode.Thread -> {
                    messageListHeaderViewModel.setActiveThread(it.parentMessage)
                    messageInputViewModel.setActiveThread(it.parentMessage)
                }
                is MessageListViewModel.Mode.Normal -> {
                    messageListHeaderViewModel.resetThread()
                    messageInputViewModel.resetThread()
                }
            }
        }
        messageListView?.setMessageEditHandler(messageInputViewModel::postMessageToEdit)
    }
}

@StreamComponents
@Suppress("UNCHECKED_CAST")
fun <T : StreamUIComponent> LifecycleOwner.streamMessageListComponent(
    messageIdProvider: (() -> String)? = null,
    cidProvider: () -> String,
): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) {
        StreamMessageListUIComponent(
            lifecycleOwner = this,
            messageId = messageIdProvider?.invoke(),
            cid = cidProvider.invoke(),
        ) as T
    }
}
