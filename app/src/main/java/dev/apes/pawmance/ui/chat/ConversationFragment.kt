package dev.apes.pawmance.ui.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentConversationBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.model.PetTokenData
import dev.apes.pawmance.utils.repeatOnLifecycle
import dev.apes.pawmance.utils.textChangeListener
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ConversationFragment : Fragment(R.layout.fragment_conversation) {

  private val viewModel by viewModels<ConversationViewModel>()
  private val binding by viewBinding(FragmentConversationBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // remove token data
    arguments?.let { bundle ->
      val petId = bundle.getString("petId") ?: return
      val tokenId = bundle.getString("tokenId")
      viewModel.getConversations(PetTokenData(petId, tokenId))
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setListeners()

    val conversationAdapter = ConversationsAdapter()
    binding.conversationList.adapter = conversationAdapter
    binding.conversationList.layoutManager = ConversationLayoutManager(requireContext())

    binding.conversationList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        binding.toolbar.elevation = if (recyclerView.canScrollVertically(-1)) 4f else 0f
      }
    })

    repeatOnLifecycle {
      viewModel.conversations.collect(conversationAdapter::submitList)
    }
  }

  private fun setListeners() {
    binding.messageEditText.textChangeListener { message ->
      viewModel.sendMessage(message)
      setMessageButtonState(message.isEmpty())
    }

    binding.sendMessageButton.setOnClickListener {
      viewModel.onMessageSend()
      binding.messageEditText.text.clear()
    }

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }
  }

  private fun setMessageButtonState(isEmpty: Boolean) {
    binding.sendMessageButton.alpha = if (isEmpty) 0.5f else 1f
    binding.sendMessageButton.isEnabled = !isEmpty
  }
}