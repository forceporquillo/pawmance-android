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

  private val _locationFlow = MutableStateFlow("")
  val locationFlow = _locationFlow.asStateFlow()

  private val _birthdateFlow = MutableStateFlow("")
  val birthdateFlow = _birthdateFlow.asStateFlow()

  private val _preferenceFlow = MutableStateFlow("")
  val preferenceFlow = _preferenceFlow.asStateFlow()

  private val _genderFlow = MutableStateFlow("")
  val genderFlow = _genderFlow.asStateFlow()

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

  var currentLocation = _locationFlow.value
    set(value) {
      _locationFlow.value = value
      field = value
    }

  var birthdate = _birthdateFlow.value
    set(value) {
      _birthdateFlow.value = value
      field = value
    }

  var preference = _preferenceFlow.value
    set(value) {
      _preferenceFlow.value = value
      field = value
    }

  var gender = _genderFlow.value
    set(value) {
      _genderFlow.value = value
      field = value
    }


  fun onContinueClick() {
    viewModelScope.launch {
      val pet =
        PetName(
          signInViewModelDelegate.userIdValue!!,
          _petName.value,
          _breedName.value,
          birthdate,
          preference,
          gender
        )
      petInfoRepository.addPetName(pet).collect(::filterResult)
    }
  }

  fun myLocation(myLocation: MyLocation) {
    Timber.e("MyLocation $myLocation")
  }
}