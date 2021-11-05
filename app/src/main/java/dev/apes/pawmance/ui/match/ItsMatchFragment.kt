package dev.apes.pawmance.ui.match

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentItsMatchBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.ui.profile.PetProfileViewModel
import dev.apes.pawmance.utils.bindImageWith
import dev.apes.pawmance.utils.navigate
import dev.apes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ItsMatchFragment : Fragment(R.layout.fragment_its_match) {

  private val profileViewModel by viewModels<PetProfileViewModel>()
  private val binding by viewBinding(FragmentItsMatchBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.backToSwipe.setOnClickListener {
      findNavController().navigateUp()
    }

    val petName = argumentExtra("petName")
    binding.matchMessage.text =
      getString(R.string.you_have_liked_each_other, petName)

    val petProfile = argumentExtra("petProfile")
    bindImageWith(binding.myMatchContainer, petProfile)

    repeatOnLifecycle {
      profileViewModel.petInfo.collect {
        bindImageWith(binding.mySelfProfile, it?.petPrimaryProfile())
      }
    }

    binding.sendMessage.setOnClickListener {
      val petId = argumentExtra("petId")
      val tokenId = argumentExtra("tokenId")
      navigate(
        R.id.action_itsMatchFragment_to_conversationFragment,
        bundleOf("petId" to petId, "tokenId" to tokenId)
      )
    }
  }

  private fun argumentExtra(type: String): String? {
    return arguments?.getString(type)
  }
}