package dev.forcecodes.pawmance.ui.preference

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.pawmance.data.info.PetInfoRepository
import dev.forcecodes.pawmance.ui.registration.CompletePetDetailsViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.LinkedList
import javax.inject.Inject

@HiltViewModel
class PetPreferenceViewModel @Inject constructor(
  private val petInfoRepository: PetInfoRepository
) : CompletePetDetailsViewModel() {

  val toMainNavActions = mUiEvents.receiveAsFlow()

  private val breedPreferences: LinkedList<String> = LinkedList()

  fun addPreference(isChecked: Boolean, prefs: String) {
    if (!breedPreferences.contains(prefs)) {
      breedPreferences.add(prefs)
    }
    if (!isChecked) {
      breedPreferences.remove(prefs)
    }

    breedPreferences.forEach {
      Timber.e("Preferences $it")
    }

    enableSubmitButton(breedPreferences.isNotEmpty())
  }

  fun onContinueClick() {
    viewModelScope.launch {
      petInfoRepository.addPetPreferences(
        breedPreferences,
        signInViewModelDelegate.userIdValue!!
      ).collect(::filterResult)
    }
  }
}