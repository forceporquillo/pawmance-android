package dev.apes.pawmance.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.data.auth.PetInfo
import dev.apes.pawmance.data.auth.PetInfoStateDataSource
import dev.apes.pawmance.data.conversation.StreamClientConnector
import dev.apes.pawmance.data.info.PetInfoRepository
import dev.apes.pawmance.ui.signin.SignInViewModelDelegate
import dev.apes.pawmance.utils.Result
import dev.apes.pawmance.utils.successOr
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.Timber.Forest
import javax.inject.Inject

@HiltViewModel
class PetProfileViewModel @Inject constructor(
  private val petInfoStateDataSource: PetInfoStateDataSource,
  private val petInfoRepository: PetInfoRepository,
  private val streamClientConnector: StreamClientConnector,
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

  private var called = false

  fun load() {
    viewModelScope.launch {
      userId.map {
        petInfoStateDataSource.getCollectionInfo(it ?: return@map).collect { result ->
          _petInfo.value = result.successOr(null).also { petInfo ->
            if (!called) {
              streamClientConnector.connectUser(it, petInfo ?: return@collect).first()
              called = true
            }
          }
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

  override fun onCleared() {
    super.onCleared()
    streamClientConnector.disconnectUser(userIdValue ?: return)
  }
}