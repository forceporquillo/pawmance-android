package dev.apes.pawmance.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.R.layout
import com.devforcecodes.pawmance.databinding.FragmentEditInfoBinding
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.binding.viewBinding
import dev.apes.pawmance.ui.location.MyLocationViewModel
import dev.apes.pawmance.ui.location.setQueryAdapter
import dev.apes.pawmance.ui.pet.PetNameViewModel
import dev.apes.pawmance.utils.Constants
import dev.apes.pawmance.utils.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class EditPetInfoFragment : Fragment(R.layout.fragment_edit_info) {

  private val viewModel by viewModels<PetProfileViewModel>()
  private val locationViewModel by viewModels<MyLocationViewModel>()
  private val petNameViewModel by activityViewModels<PetNameViewModel>()

  private val binding by viewBinding(FragmentEditInfoBinding::bind)

  private fun createAdapter(items: Array<String>) =
    ArrayAdapter(requireContext(), layout.dropdown_menu_popup, items)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    repeatOnLifecycle {
      viewModel.petInfo.collect {
        binding.petNameEditText.setText(it?.petName())
        binding.breedEditText.setText(it?.petBreed())
        binding.currentLocationEditText.setText(it?.location()?.placeName)
        binding.birthdateEditText.setText(it?.petBirthdate())
        binding.genderEditText.setText(it?.petGender())
        binding.prefereceEditText.setText(it?.petPreferences()?.joinToString { it })

        binding.breedEditText.apply {
          addTextChangedListener {
            petNameViewModel.breedName = it.toString()
          }
        }

        binding.breedEditText.setAdapter(createAdapter(Constants.BREEDS))
        binding.genderEditText.setAdapter(createAdapter(Constants.GENDER))
        binding.prefereceEditText.setAdapter(createAdapter(Constants.PREFERENCES))
      }
    }

    binding.updateProfileButton.setOnClickListener {
      petNameViewModel.onContinueClick()
      repeatOnLifecycle {
        delay(850L)
        findNavController().navigateUp()
      }
    }

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }

    binding.petNameEditText.addTextChangedListener {
      petNameViewModel.petName = it.toString()
    }

    binding.birthdateEditText.setOnClickListener { showDatePicker() }
    binding.birthdateEditText.setOnItemClickListener { _, _, _, _ -> showDatePicker() }

    binding.birthdateEditText.addTextChangedListener {
      petNameViewModel.birthdate = it.toString()
    }

    binding.prefereceEditText.addTextChangedListener {
      petNameViewModel.preference = it.toString()
    }

    binding.genderEditText.addTextChangedListener {
      petNameViewModel.gender = it.toString()
    }

    binding.currentLocationEditText.apply {
      setOnItemClickListener { _, _, index, _ ->
        val prediction = locationViewModel.placesResults.value[index]
        locationViewModel.getMyLocationData(prediction)
      }

      addTextChangedListener { query ->
        locationViewModel.searchPlace(query.toString())
      }

      repeatOnLifecycle {
        locationViewModel.placesResults.collectLatest { predictions ->
          val queryAdapter =
            setQueryAdapter(predictions.map { it?.description ?: "" })
          setAdapter(queryAdapter)
          showDropDown()
        }
      }
    }
  }

  private fun showDatePicker() {
    val datePicker =
      MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select date")
        .build()

    datePicker.show(childFragmentManager, "date_picker")
    datePicker.addOnPositiveButtonClickListener {
      binding.birthdateEditText.setText(datePicker.headerText)
    }
  }
}