package dev.apes.pawmance.ui.location

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.apes.pawmance.data.info.PetInfoRepository
import dev.apes.pawmance.data.location.LocationSearchRepository
import dev.apes.pawmance.model.Coordinates
import dev.apes.pawmance.model.MyLocation
import dev.apes.pawmance.model.PlaceAutoCompleteResponse
import dev.apes.pawmance.model.Prediction
import dev.apes.pawmance.ui.registration.CompletePetDetailsViewModel
import dev.apes.pawmance.utils.Result
import dev.apes.pawmance.utils.cancelIfActive
import dev.apes.pawmance.utils.successOr
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyLocationViewModel @Inject constructor(
  private val locationSearchRepository: LocationSearchRepository,
  private val petInfoRepository: PetInfoRepository
) : CompletePetDetailsViewModel() {

  private val _placesResults = MutableStateFlow<List<Prediction?>>(emptyList())
  val placesResults = _placesResults.asStateFlow()

  private val placeQuery = MutableStateFlow("")

  private var searchJob: Job? = null

  fun searchPlace(place: String) {
    val newQuery = place.trim().takeIf { it.length >= 2 } ?: ""

    var skipSearch = false

    for (prediction in _placesResults.value) {
      if (prediction?.description == place) {
        skipSearch = true
        Timber.e("isExist")
        break
      }
    }

    if (skipSearch) {
      return
    }

    if (placeQuery.value != newQuery) {
      placeQuery.value = newQuery
      executeSearch()
    }
  }

  private fun executeSearch() {
    searchJob?.cancelIfActive()

    if (placeQuery.value.isEmpty()) {
      clearSearchResults()
      return
    }

    searchJob = viewModelScope.launch {
      placeQuery
        .debounce(250)
        .collect { query ->
          locationSearchRepository
            .getAutocompletePlaces(query)
            .collect(::processSearchResult)
        }
    }
  }

  fun getMyLocationData(prediction: Prediction?) {
    viewModelScope.launch {
      getMyCompleteLocationData(
        prediction?.placeId,
        prediction?.description
      ).collect(::addMyLocation)
    }
  }

  private suspend fun getMyCompleteLocationData(
    placeId: String?,
    placeName: String?
  ): Flow<MyLocation> = flow {
    locationSearchRepository
      .getPlaceDetails(placeId ?: return@flow)
      .collect { result ->
        when (result) {
          is Result.Error -> Timber.e(result.exception)
          is Result.Success -> {
            val location = result.data.result?.geometry?.location
            emit(
              MyLocation(
                placeId,
                Coordinates(location?.lat, location?.lng),
                placeName
              )
            )
          }
          Result.Loading -> {
          }
        }
      }
  }.stateIn(viewModelScope)

  private fun addMyLocation(myLocation: MyLocation) {
    viewModelScope.launch {
      petInfoRepository.addPetLocation(
        signInViewModelDelegate.userIdValue!!,
        myLocation
      ).collect { result ->
        enableSubmitButton(result.successOr(false))
        filterResult(result)
      }
    }
  }

  private fun processSearchResult(result: Result<PlaceAutoCompleteResponse>) {
    if (result is Result.Success) {
      val response = result.data.predictions
      if (response != _placesResults.value) {
        _placesResults.value = response ?: listOf()
      }
    }
    if (result is Result.Error) {
      result.exception
    }
  }

  private fun clearSearchResults() {
    _placesResults.value = listOf()
  }
}