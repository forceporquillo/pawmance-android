package dev.forcecodes.pawmance.ui.photos

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.pawmance.data.info.PetInfoRepository
import dev.forcecodes.pawmance.ui.registration.CompletePetDetailsViewModel
import dev.forcecodes.pawmance.utils.FixedByteArrayStack
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetPhotosViewModel @Inject constructor(
  private val petInfoRepository: PetInfoRepository
) : CompletePetDetailsViewModel() {

  val petPhotosNavAction = mUiEvents.receiveAsFlow()

  private val photoStack = FixedByteArrayStack()

  fun addBitmapImage(byteArray: ByteArray, index: Int) {
    if (photoStack.size > index) {
      photoStack[index] = byteArray
    } else {
      photoStack.add(byteArray)
    }

    enableSubmitButton(photoStack.isNotEmpty())
  }

  fun onClickAdd() {
    viewModelScope.launch {
      petInfoRepository.addPetPhotos(photoStack, signInViewModelDelegate.userIdValue!!)
        .collect(::filterResult)
    }
  }
}