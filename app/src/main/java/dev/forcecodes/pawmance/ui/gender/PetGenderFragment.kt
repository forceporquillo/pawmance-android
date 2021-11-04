package dev.forcecodes.pawmance.ui.gender

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentPetGenderBinding
import dev.forcecodes.pawmance.binding.viewBinding
import dev.forcecodes.pawmance.ui.registration.AddPetDetailsUiEvent
import dev.forcecodes.pawmance.utils.navigate
import dev.forcecodes.pawmance.utils.repeatOnLifecycleLaunch
import dev.forcecodes.pawmance.utils.showLoading
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PetGenderFragment : Fragment(R.layout.fragment_pet_gender) {

  private val viewModel by activityViewModels<PetGenderViewModel>()
  private val binding by viewBinding(FragmentPetGenderBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.maleGender.setOnClickListener { setSelected(true) }
    binding.femaleGender.setOnClickListener { setSelected(false) }
    binding.continueButton.setOnClickListener { viewModel.onContinue() }
  }

  private fun setSelected(isMale: Boolean) {
    binding.femaleRadio.isChecked = !isMale
    binding.maleRadio.isChecked = isMale

    viewModel.petGender = if (isMale) "Male" else "Female"

    repeatOnLifecycleLaunch {
      launch {
        viewModel.addPetUiState.collect {
          showLoading(it.isLoading)
        }
      }
      launch {
        viewModel.petLocationNavAction.collect {
          if (it is AddPetDetailsUiEvent.ProceedActionEvent) {
            navigate(R.id.action_petGenderFragment_to_myLocationFragment)
          }
        }
      }
    }
  }
}

