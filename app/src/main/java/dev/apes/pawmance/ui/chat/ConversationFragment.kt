package dev.apes.pawmance.ui.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentConversationBinding
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.model.PetTokenData
import dev.apes.pawmance.stream.components.StreamMessageListUIComponent
import dev.apes.pawmance.stream.components.streamMessageListComponent
import dev.apes.pawmance.utils.bindImageWith
import dev.apes.pawmance.utils.repeatOnLifecycle
import dev.apes.pawmance.utils.repeatOnLifecycleLaunch
import dev.apes.pawmance.utils.textChangeListener
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatClient.Companion
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

@AndroidEntryPoint
class ConversationFragment : Fragment(R.layout.fragment_conversation) {

  private val viewModel by viewModels<ConversationViewModel>()
  private val binding by viewBinding(FragmentConversationBinding::bind)

  // private val streamMessageListComponent: StreamMessageListUIComponent by streamMessageListComponent(
  //   cidProvider = { arguments?.getString("channel_id") ?: throw IllegalStateException() }
  // )

  private var streamMessageListUIComponent: StreamMessageListUIComponent? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // // remove token data
    // arguments?.let { bundle ->
    //   val petId = bundle.getString("channel_id") ?: return
    //   //val tokenId = bundle.getString("tokenId")
    // //  viewModel.getConversations(PetTokenData(petId, tokenId))
    // }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setListeners()

    val channelId = arguments?.getString("channel_id")
    if (channelId != null) {
      streamMessageListUIComponent = StreamMessageListUIComponent(this, cid = channelId)
      streamMessageListUIComponent?.bindLayout(binding.root)
    } else {
      val petId = arguments?.getString("pet_id") as String
      repeatOnLifecycle {
        viewModel.getChannel(petId).collect { channel ->
          streamMessageListUIComponent = StreamMessageListUIComponent(this, cid = channel.cid)
          streamMessageListUIComponent?.bindLayout(binding.root)
        }
      }
    }

    // val conversationAdapter = ConversationsAdapter()
    // binding.conversationList.adapter = conversationAdapter
    // binding.conversationList.layoutManager = ConversationLayoutManager(requireContext())
    //
    // repeatOnLifecycleLaunch {
    //   launch {
    //     viewModel.profileImage.collect {
    //       bindImageWith(binding.profileAvatar, it ?: return@collect)
    //     }
    //   }
    //   launch {
    //     viewModel.recipientName.collect {
    //       binding.userName.text = it ?: return@collect
    //     }
    //   }
    // }
    //
    // binding.conversationList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    //   override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    //     super.onScrolled(recyclerView, dx, dy)
    //
    //     binding.toolbar.elevation = if (recyclerView.canScrollVertically(-1)) 4f else 0f
    //   }
    // })
    //
    // repeatOnLifecycle {
    //   viewModel.conversations.collect(conversationAdapter::submitList)
    // }

    binding.apply {
      messageListHeaderView.setBackButtonClickListener(backHandler)
    }
  }

  private fun setListeners() {
    // binding.messageEditText.textChangeListener { message ->
    //   viewModel.sendMessage(message)
    //   setMessageButtonState(message.isEmpty())
    // }
    //
    // binding.sendMessageButton.setOnClickListener {
    //   viewModel.onMessageSend()
    //   binding.messageEditText.text.clear()
    // }
    //
    // binding.toolbar.setNavigationOnClickListener {
    //   findNavController().navigateUp()
    // }
  }

  private fun setMessageButtonState(isEmpty: Boolean) {
    // binding.sendMessageButton.alpha = if (isEmpty) 0.5f else 1f
    // binding.sendMessageButton.isEnabled = !isEmpty
  }

  private val backHandler = {
    findNavController().popBackStack()
    streamMessageListUIComponent?.messageListViewModel?.onEvent(MessageListViewModel.Event.BackButtonPressed)
    Unit
  }
}