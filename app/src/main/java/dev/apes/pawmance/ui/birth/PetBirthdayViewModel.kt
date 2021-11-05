package dev.apes.pawmance.ui.birth

import android.icu.util.Calendar
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.data.info.PetInfoRepository
import dev.apes.pawmance.ui.registration.CompletePetDetailsViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetBirthdayViewModel @Inject constructor(
  private val petInfoRepository: PetInfoRepository
) : CompletePetDetailsViewModel() {

  val petGenderNavAction = mUiEvents.receiveAsFlow()

  var month = Calendar.getInstance()
    .get(Calendar.MONTH)

  var year = Calendar.getInstance()
    .get(Calendar.YEAR)

  var day: String? = null
    set(value) {
      enableSubmitButton(value?.isNotEmpty() == true)
      field = value
    }

  var yearField: String = ""
  var monthField: String = ""

  fun onContinueClick() {
    viewModelScope.launch {
      petInfoRepository.addPetBirthdate(
        signInViewModelDelegate.userIdValue!!,
        "$monthField $day, $yearField"
      ).collect(::filterResult)
    }
  }
}