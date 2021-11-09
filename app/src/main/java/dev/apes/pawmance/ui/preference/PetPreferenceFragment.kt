package dev.apes.pawmance.ui.preference

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentPetPreferenceBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.ui.MainActivity
import dev.apes.pawmance.ui.registration.AddPetDetailsUiEvent
import dev.apes.pawmance.utils.createIntent
import dev.apes.pawmance.utils.repeatOnLifecycleLaunch
import dev.apes.pawmance.utils.showLoading
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PetPreferenceFragment : Fragment(R.layout.fragment_pet_preference) {

  private val binding by viewBinding(FragmentPetPreferenceBinding::bind)
  private val viewModel by viewModels<PetPreferenceViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = viewModel

    val chipViews = binding.run { arrayOf(withPapers, withoutPapers) }

    chipViews.forEach { chip ->
      chip.setOnCheckedChangeListener { compoundButton, isChecked ->
        compoundButton.isChecked = isChecked

        viewModel.addPreference(isChecked, compoundButton.text.toString())
      }
    }

    showLoadingState()
  }

  private fun showLoadingState() = repeatOnLifecycleLaunch {
    launch {
      viewModel.addPetUiState.collect {
        showLoading(it.isLoading)
      }
    }
    launch {
      viewModel.toMainNavActions.collect { uiEvent ->
        if (uiEvent is AddPetDetailsUiEvent.ProceedActionEvent) {
          createIntent(MainActivity::class)
          requireActivity().finish()
        }
      }
    }
  }
}

