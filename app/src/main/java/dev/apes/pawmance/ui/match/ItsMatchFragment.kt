package dev.apes.pawmance.ui.match

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentItsMatchBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.data.PushNotificationManager
import dev.apes.pawmance.data.auth.PetInfoStateDataSource
import dev.apes.pawmance.model.MessageDataV2
import dev.apes.pawmance.ui.profile.PetProfileViewModel
import dev.apes.pawmance.ui.signin.SignInViewModelDelegate
import dev.apes.pawmance.utils.Result
import dev.apes.pawmance.utils.bindImageWith
import dev.apes.pawmance.utils.navigate
import dev.apes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.Forest
import javax.inject.Inject

@AndroidEntryPoint
class ItsMatchFragment : Fragment(R.layout.fragment_its_match) {

  private val profileViewModel by viewModels<PetProfileViewModel>()
  private val binding by viewBinding(FragmentItsMatchBinding::bind)
  private val pushNotificationViewModel by viewModels<MatchNotificationViewModel>()

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

    val tokenId = argumentExtra("tokenId")

    if (!tokenId.isNullOrEmpty()) {
      pushNotificationViewModel.pushMatch(tokenId)
    }

    binding.sendMessage.setOnClickListener {
      val petId = argumentExtra("petId")
      val tokenId = argumentExtra("tokenId")
      navigate(
        R.id.action_itsMatchFragment_to_conversationFragment,
        bundleOf("pet_id" to petId, "channel_id" to null)
      )
    }
  }

  private fun argumentExtra(type: String): String? {
    return arguments?.getString(type)
  }
}

@HiltViewModel
class MatchNotificationViewModel @Inject constructor(
  private val pushNotificationManager: PushNotificationManager,
  private val signInViewModelDelegate: SignInViewModelDelegate,
  private val petInfoStateDataSource: PetInfoStateDataSource
): ViewModel(), SignInViewModelDelegate by signInViewModelDelegate {

  fun pushMatch(tokenId: String) {
    viewModelScope.launch {
      signInViewModelDelegate.userId.map { uid ->
        petInfoStateDataSource.getCollectionInfo(uid ?: return@map).collect { result ->
          if (result is Result.Success) {
            pushNotificationManager.notifyBreedMatch(
                tokenId, MessageDataV2(result.data?.petName().orEmpty())
            )
          }
        }
      }.collect()
    }
  }

  fun notifyYouLike(tokenId: String) {
    Timber.e("Notifying you like...")
    viewModelScope.launch {
      signInViewModelDelegate.userId.map { uid ->
        petInfoStateDataSource.getCollectionInfo(uid ?: return@map).collect { result ->
          if (result is Result.Success) {
            pushNotificationManager.notifyBreedMatch(
              tokenId, MessageDataV2(youLike = result.data?.petName().orEmpty())
            )
          }
        }
      }.collect()
    }
  }
}