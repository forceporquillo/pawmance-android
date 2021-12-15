package dev.apes.pawmance.ui.chat

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.stream.components.streamChannelListComponent
import dev.apes.pawmance.utils.navigate
import dev.apes.pawmance.utils.repeatOnLifecycle
import dev.apes.pawmance.stream.components.StreamUIComponent
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ChatChannelListFragment : Fragment(R.layout.fragment_chat) {

  private val viewModel by viewModels<ChatViewModel>()
  private val binding by viewBinding(FragmentChatBinding::bind)
  private val streamChannelListUIComponent: StreamUIComponent by streamChannelListComponent()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.toString()

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }
//
    val chatAdapter = ChatAdapter()

    // binding.chatListItem.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    //   override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    //     super.onScrolled(recyclerView, dx, dy)
    //
    //     binding.toolbar.elevation = if (recyclerView.canScrollVertically(-1)) 4f else 0f
    //   }
    // })

    // binding.chatListItem.recycledViewPool.setMaxRecycledViews(ConversationsAdapter.SENDER, 0)
    // binding.chatListItem.recycledViewPool.setMaxRecycledViews(ConversationsAdapter.RECEIVER, 0)
    //
    // binding.chatListItem.adapter = chatAdapter
    // binding.chatListItem.addItemDecoration(
    //   DividerItemDecoration(
    //     requireContext(),
    //     LinearLayoutManager.VERTICAL
    //   )
    // )

    streamChannelListUIComponent.bindLayout(binding.root)

    repeatOnLifecycle {
      viewModel.recentConversations.collect(chatAdapter::submitList)
    }

    binding.channelListView.setChannelItemClickListener { channel ->
      navigate(R.id.action_chatFragment_to_conversationFragment, bundleOf("channel_id" to channel.cid))
    }

    chatAdapter.onConversationClick = {
      navigate(R.id.action_chatFragment_to_conversationFragment, bundleOf("petId" to it))
    }
  }
}

