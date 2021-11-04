package dev.forcecodes.pawmance.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.pawmance.data.auth.PetInfo
import dev.forcecodes.pawmance.data.auth.PetInfoStateDataSource
import dev.forcecodes.pawmance.data.info.PetInfoRepository
import dev.forcecodes.pawmance.ui.signin.SignInViewModelDelegate
import dev.forcecodes.pawmance.utils.Result
import dev.forcecodes.pawmance.utils.successOr
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetProfileViewModel @Inject constructor(
  private val petInfoStateDataSource: PetInfoStateDataSource,
  private val petInfoRepository: PetInfoRepository,
  signInViewModelDelegate: SignInViewModelDelegate
) : ViewModel(), SignInViewModelDelegate by signInViewModelDelegate {

  private val _petInfo = MutableStateFlow<PetInfo?>(null)
  val petInfo = _petInfo.asStateFlow()

  private val _uploadResult = MutableStateFlow<UploadTask?>(null)
  val uploadTask = _uploadResult.asStateFlow()

  data class UploadTask(
    val isLoading: Boolean? = null,
    val isSuccess: Boolean? = null,
    val exception: Exception? = null,
    val data: Uri? = null
  )

  init {
    viewModelScope.launch {
      signInViewModelDelegate.userId.map {
        petInfoStateDataSource.getCollectionInfo(it ?: return@map).collect { result ->
          _petInfo.value = result.successOr(null)
        }
      }.collect()
    }
  }

  fun addPetProfile(uri: Uri) {
    viewModelScope.launch {
      petInfoRepository.addPetProfilePhoto(uri, userIdValue!!).collect { result ->
        _uploadResult.value = when (result) {
          is Result.Loading -> UploadTask(true)
          is Result.Success -> UploadTask(isLoading = false, true, data = result.data)
          is Result.Error -> UploadTask(exception = result.exception)
        }
      }
    }
  }

  fun updateBreed(breed: String) {
    viewModelScope.launch {
      userId.flatMapConcat { userId ->
        petInfoRepository.updatePetBreed(userId ?: "", breed)
      }
    }
  }
}