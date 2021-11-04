package dev.forcecodes.pawmance.ui.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.forcecodes.pawmance.data.GaleShapelyMatchDataSource
import dev.forcecodes.pawmance.data.info.PetInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MatchPetViewModel @Inject constructor(
  private val galeShapelyMatchDataSource: GaleShapelyMatchDataSource,
  private val petInfoRepository: PetInfoRepository
) : ViewModel() {

  private val _petList = MutableStateFlow<List<Pet?>>(emptyList())
  val petList = _petList.asStateFlow()

  private var myId: String? = null

  fun getMatches(id: String?) {
    if (id.isNullOrEmpty()) {
      return
    }
    myId = id

    viewModelScope.launch {
      galeShapelyMatchDataSource.getMyPossibleMatches(id)
        .collect {
          _petList.value = it
        }
    }
  }

  fun setLikedPet(partnerId: String) {
    viewModelScope.launch {
      petInfoRepository.addPartnerId(myId ?: return@launch, partnerId)
        .collect {
          Timber.e(it.toString())
        }
    }
  }
}