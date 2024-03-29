package dev.apes.pawmance.ui.pet

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.R.layout
import com.devforcecodes.pawmance.databinding.FragmentPetNameBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.ui.registration.AddPetDetailsUiEvent
import dev.apes.pawmance.utils.Constants
import dev.apes.pawmance.utils.navigate
import dev.apes.pawmance.utils.repeatOnLifecycleLaunch
import dev.apes.pawmance.utils.showLoading
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PetNameFragment : Fragment(R.layout.fragment_pet_name) {

  private val viewModel by activityViewModels<PetNameViewModel>()
  private val binding by viewBinding(FragmentPetNameBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewModel = viewModel

    with(binding) {
      val adapter =
        ArrayAdapter(requireContext(), layout.dropdown_menu_popup, Constants.BREEDS)
      breedEditText.setAdapter(adapter)

      breedEditText.addTextChangedListener { viewModel!!.breedName = it.toString() }
      petNameEditText.addTextChangedListener { viewModel!!.petName = it.toString() }
    }

    observeChanges()
  }

  private fun observeChanges() = repeatOnLifecycleLaunch {
    launch {
      viewModel.toPetBirthdayNavAction.collect { uiEvent ->
        showLoading(false)
        if (uiEvent is AddPetDetailsUiEvent.ProceedActionEvent) {
          navigate(R.id.action_nameRegistrationFragment_to_petBirthdayFragment)
        }
      }
    }
    launch {
      viewModel.addPetUiState.collect {
        showLoading(it.isLoading)
        binding.petNameTextLayout.error = it.errorMessage
      }
    }
  }
}
