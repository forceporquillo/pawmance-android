package dev.forcecodes.pawmance.ui.chat

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.pawmance.binding.viewBinding
import dev.forcecodes.pawmance.utils.navigate
import dev.forcecodes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {

  private val viewModel by viewModels<ChatViewModel>()
  private val binding by viewBinding(FragmentChatBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.toString()

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }

    val chatAdapter = ChatAdapter()

    binding.chatListItem.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        binding.toolbar.elevation = if (recyclerView.canScrollVertically(-1)) 4f else 0f
      }
    })

    binding.chatListItem.adapter = chatAdapter
    binding.chatListItem.addItemDecoration(
      DividerItemDecoration(
        requireContext(),
        LinearLayoutManager.VERTICAL
      )
    )

    repeatOnLifecycle {
      viewModel.recentConversations.collect(chatAdapter::submitList)
    }

    chatAdapter.onConversationClick = {
      navigate(R.id.action_chatFragment_to_conversationFragment, bundleOf("petId" to it))
    }
  }
}

