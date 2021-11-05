package dev.apes.pawmance.ui.pet

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.data.info.PetInfoRepository
import dev.apes.pawmance.model.MyLocation
import dev.apes.pawmance.model.PetName
import dev.apes.pawmance.ui.registration.CompletePetDetailsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PetNameViewModel @Inject constructor(
  private val petInfoRepository: PetInfoRepository
) : CompletePetDetailsViewModel() {

  val toPetBirthdayNavAction = mUiEvents.receiveAsFlow()

  private val _petName = MutableStateFlow("")
  val petNameFlow = _petName.asStateFlow()

  private val _breedName = MutableStateFlow("")
  val breedNameFlow = _breedName.asStateFlow()

  init {
    viewModelScope.launch {
      _petName.collect { enableSubmitButton(it.isNotEmpty()) }
      _breedName.collect { enableSubmitButton(it.isNotEmpty()) }
    }
  }

  var petName: String = _petName.value
    set(value) {
      _petName.value = value
      field = value
    }

  var breedName = _breedName.value
    set(value) {
      _breedName.value = value
      field = value
    }

  fun onContinueClick() {
    viewModelScope.launch {
      val pet =
        PetName(signInViewModelDelegate.userIdValue!!, _petName.value, _breedName.value)
      petInfoRepository.addPetName(pet).collect(::filterResult)
    }
  }

  fun myLocation(myLocation: MyLocation) {
    Timber.e("MyLocation $myLocation")
  }
}