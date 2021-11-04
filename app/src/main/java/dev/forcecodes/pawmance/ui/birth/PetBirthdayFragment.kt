package dev.forcecodes.pawmance.ui.birth

import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.devforcecodes.pawmance.R
import com.devforcecodes.pawmance.databinding.FragmentPetBirthdayBinding
import dev.forcecodes.pawmance.binding.viewBinding
import dev.forcecodes.pawmance.ui.registration.AddPetDetailsUiEvent
import dev.forcecodes.pawmance.utils.Constants
import dev.forcecodes.pawmance.utils.createAdapter
import dev.forcecodes.pawmance.utils.navigate
import dev.forcecodes.pawmance.utils.repeatOnLifecycleLaunch
import dev.forcecodes.pawmance.utils.showLoading
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PetBirthdayFragment : Fragment(R.layout.fragment_pet_birthday) {

  private val petBirthdayViewModel by activityViewModels<PetBirthdayViewModel>()
  private val binding by viewBinding(FragmentPetBirthdayBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.viewModel = petBirthdayViewModel
    binding.lifecycleOwner = viewLifecycleOwner

    initViews()
    observeChanges()
  }

  private fun initViews() = with(binding) {
    continueButton.setOnClickListener {
      navigate(R.id.action_petBirthdayFragment_to_petGenderFragment)
    }

    monthEditText.setAdapter(createAdapter(Constants.MONTHS))
    yearEditText.setAdapter(createAdapter(Constants.YEARS))

    monthEditText.addTextChangedListener {
      if (it.toString().isNotEmpty()) {
        for ((index, month) in Constants.MONTHS.withIndex()) {
          if (month == it.toString()) {
            redrawDays(numberOfDays(month = index))
            petBirthdayViewModel.monthField = Constants.MONTHS[index]
          }
        }
      }
    }

    dayOfMonthEditText.addTextChangedListener {
      petBirthdayViewModel.day = it.toString()
    }

    yearEditText.addTextChangedListener { year ->
      if (year.toString().isNotEmpty()) {
        val yearInt = year.toString().toInt()
        petBirthdayViewModel.year = yearInt
        petBirthdayViewModel.yearField = year.toString()
        redrawDays(numberOfDays(year = year.toString().toInt()))
      }
    }
  }

  private fun observeChanges() = repeatOnLifecycleLaunch {
    launch {
      petBirthdayViewModel.petGenderNavAction.collect { uiEvent ->
        showLoading(false)
        if (uiEvent is AddPetDetailsUiEvent.ProceedActionEvent) {
          navigate(R.id.action_petBirthdayFragment_to_petGenderFragment)
        }
      }
    }
    launch {
      petBirthdayViewModel.addPetUiState.collect {
        showLoading(it.isLoading)
        binding.errorTextMessage.text = it.errorMessage
      }
    }
  }

  private fun redrawDays(daysInMonths: Int) {
    with(binding.dayOfMonthEditText) {
      val days = (1..daysInMonths).map { m -> m.toString() }.toTypedArray()
      setAdapter(createAdapter(days))
    }
  }

  private fun numberOfDays(year: Int? = null, month: Int? = null): Int {
    return Calendar.getInstance().run {
      set(Calendar.YEAR, year ?: petBirthdayViewModel.year)
      set(Calendar.MONTH, month ?: petBirthdayViewModel.month)
      getActualMaximum(Calendar.DATE)
    }
  }
}