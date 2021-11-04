package dev.forcecodes.pawmance.ui.match

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentMatchPetBinding
import com.devforcecodes.pawmance.databinding.PetSwipeItemBinding
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.RewindAnimationSetting
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting
import dagger.hilt.android.AndroidEntryPoint
import dev.forcecodes.pawmance.binding.viewBinding
import dev.forcecodes.pawmance.ui.profile.PetProfileViewModel
import dev.forcecodes.pawmance.utils.navigate
import dev.forcecodes.pawmance.utils.repeatOnLifecycleLaunch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MatchPetFragment : Fragment(R.layout.fragment_match_pet), CardStackListener {

  private val matchPetViewModel by viewModels<MatchPetViewModel>()

  private val viewModel by viewModels<PetProfileViewModel>()
  private val binding by viewBinding(FragmentMatchPetBinding::bind)

  private lateinit var manager: MatchingCardStackManager
  private lateinit var matchingSwipeAdapter: PetMatchingSwipeAdapter

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    manager = MatchingCardStackManager(requireContext(), this)
    matchingSwipeAdapter = PetMatchingSwipeAdapter()

    initialize(matchingSwipeAdapter)
    initButtonListener()

    repeatOnLifecycleLaunch {

      launch {
        viewModel.userId.collect { id ->
          matchPetViewModel.getMatches(id)
        }
      }

      launch {
        matchPetViewModel.petList.collect {
          matchingSwipeAdapter.submitList(it)
        }
      }
    }

    binding.recentMessage.setOnClickListener {
      navigate(R.id.action_matchPetFragment_to_chatFragment)
    }
  }

  private fun initialize(matchingSwipeAdapter: PetMatchingSwipeAdapter) {
    try {
      binding.cardStackView.layoutManager = manager
      binding.cardStackView.adapter = matchingSwipeAdapter
      binding.cardStackView.itemAnimator.apply {
        if (this is DefaultItemAnimator) {
          supportsChangeAnimations = false
        }
      }
    } catch (e: Exception) {

    }
  }

  override fun onCardDragging(direction: Direction, ratio: Float) {
    // Timber.d("onCardDragging: d = " + direction.name + ", r = " + ratio)
  }

  override fun onCardSwiped(direction: Direction) {
    Timber.d("onCardSwiped: p = " + manager.topPosition + ", d = " + direction)
    if (direction == Direction.Right) {
      val currentPetSwiped = matchingSwipeAdapter.getItemPos(manager.topPosition - 1)
      currentPetSwiped?.let {
        matchPetViewModel.setLikedPet(it.id)

        if (it.isMatch) {

          val bundle = Bundle().apply {
            putString("tokenId", it.tokenId)
            putString("petId", it.id)
            putString("petProfile", it.profile)
            putString("petName", it.name)
          }

          navigate(R.id.action_matchPetFragment_to_itsMatchFragment, bundle)
        }
      }
    }
  }

  override fun onCardRewound() {
    //  Timber.d("onCardRewound: " + manager.topPosition)
  }

  override fun onCardCanceled() {
    Timber.d("onCardCanceled: " + manager.topPosition)
  }

  override fun onCardAppeared(view: View, position: Int) {
    Timber.d("onCardAppeared: ($position)")
  }

  override fun onCardDisappeared(view: View, position: Int) {
    //   Timber.d("onCardDisappeared: (${manager.itemCount})")
  }

  private fun initButtonListener() {
    binding.heartButton.setOnClickListener {
      swipeAnimationSetting(Direction.Right)
    }
    binding.skipButton.setOnClickListener {
      swipeAnimationSetting(Direction.Left)
    }
    binding.rewindButton.setOnClickListener {
      val setting = RewindAnimationSetting.Builder()
        .setDirection(Direction.Bottom)
        .setDuration(Duration.Normal.duration)
        .setInterpolator(DecelerateInterpolator())
        .build()

      Timber.d("rewind")
      manager.setRewindAnimationSetting(setting)
      binding.cardStackView.rewind()
    }
  }

  private fun swipeAnimationSetting(direction: Direction) {
    val setting = SwipeAnimationSetting.Builder()
      .setDirection(direction)
      .setDuration(Duration.Slow.duration)
      .setInterpolator(AccelerateInterpolator())
      .build()
    manager.setSwipeAnimationSetting(setting)
    binding.cardStackView.swipe()
  }
}

data class Pet(
  val id: String,
  val name: String?,
  val distance: Float?,
  val breed: String?,
  val profile: String?,
  val isMatch: Boolean = false,
  val tokenId: String? = null
)

class PetMatchingSwipeAdapter : ListAdapter<Pet, PetMatchingViewHolder>(PET_COMPARATOR) {

  var onPetMatchListener: () -> Unit = {}

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetMatchingViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    val binding = PetSwipeItemBinding.inflate(inflater, parent, false)
    return PetMatchingViewHolder(binding)
  }

  override fun onBindViewHolder(holder: PetMatchingViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  fun getItemPos(position: Int): Pet? {
    return getItem(position)
  }
}

private val PET_COMPARATOR = object : DiffUtil.ItemCallback<Pet>() {
  override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean {
    return oldItem.id == newItem.id
  }

  override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean {
    return oldItem == newItem
  }
}

class PetMatchingViewHolder(
  private val binding: PetSwipeItemBinding
) : RecyclerView.ViewHolder(binding.root) {

  fun bind(pet: Pet) {

    val imageView = binding.petImagePlaceholder

    Glide.with(imageView)
      .load(pet.profile)
      .override(imageView.width, imageView.height)
      .into(imageView)

    val distance = if ((pet.distance ?: 0.0f) < 1.0f) {
      "${(pet.distance?.times(1000))?.toInt()} meters away"
    } else {
      "${pet.distance} kms away"
    }

    binding.itemName.text = pet.name
    binding.itemCity.text = distance
    binding.breedType.text = pet.breed
  }
}