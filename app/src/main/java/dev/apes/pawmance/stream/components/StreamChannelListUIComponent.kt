package dev.apes.pawmance.stream.components

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.devforcecodes.pawmance.R
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory

class StreamChannelListUIComponent constructor(
    override val lifecycleOwner: LifecycleOwner
) : StreamUIComponent {

    private val factory by lazy(LazyThreadSafetyMode.NONE) {
        ChannelListViewModelFactory()
    }

    private val channelListViewModel: ChannelListViewModel by lazy(LazyThreadSafetyMode.NONE) {
        factory.create(ChannelListViewModel::class.java)
    }

    @StreamComponents
    override fun bindLayout(view: View) {
        val channelListView =
            view.findViewById<ChannelListView>(R.id.channelListView)
        channelListView?.let {
            channelListViewModel.bindView(it, lifecycleOwner)
        }
    }
}

@StreamComponents
fun LifecycleOwner.streamChannelListComponent(): Lazy<StreamUIComponent> {
    return lazy(LazyThreadSafetyMode.NONE) {
        StreamChannelListUIComponent(lifecycleOwner = this)
    }
}
