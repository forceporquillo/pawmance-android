package dev.forcecodes.pawmance.ui.gender

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.pawmance.data.info.PetInfoRepository
import dev.forcecodes.pawmance.ui.registration.CompletePetDetailsViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetGenderViewModel @Inject constructor(
  private val petInfoRepository: PetInfoRepository
) : CompletePetDetailsViewModel() {

  val petLocationNavAction = mUiEvents.receiveAsFlow()

  var petGender: String = ""
    set(value) {
      enableSubmitButton(value.isNotEmpty())
      field = value
    }

  fun onContinue() {
    viewModelScope.launch {
      petInfoRepository.addPetGender(
        signInViewModelDelegate.userIdValue!!, petGender
      ).collect(::filterResult)
    }
  }
}