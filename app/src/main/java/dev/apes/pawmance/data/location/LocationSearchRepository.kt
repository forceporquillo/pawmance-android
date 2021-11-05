package dev.apes.pawmance.data.location

import dev.apes.pawmance.api.ApiResponse
import dev.apes.pawmance.api.PlacesApiService
import dev.apes.pawmance.data.BaseRepository
import dev.apes.pawmance.di.IoDispatcher
import dev.apes.pawmance.di.PlacesBackendApi
import dev.apes.pawmance.model.PlaceAutoCompleteResponse
import dev.apes.pawmance.model.PlaceDetailsResponse
import dev.apes.pawmance.utils.Result
import dev.apes.pawmance.utils.mapApiRequestResults
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface LocationSearchRepository {
  fun getAutocompletePlaces(placeAddress: String): Flow<Result<PlaceAutoCompleteResponse>>
  fun getPlaceDetails(placeId: String): Flow<Result<PlaceDetailsResponse>>
}

@Singleton
class LocationSearchRepositoryImpl @Inject constructor(
  @PlacesBackendApi private val apiService: PlacesApiService,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseRepository(ioDispatcher), LocationSearchRepository {

  override fun getAutocompletePlaces(
    placeAddress: String
  ): Flow<Result<PlaceAutoCompleteResponse>> =
    flow {
      emit(Result.Loading)

      val placesApiResponse = filterResult(placeAddress)
        .map { apiResponse ->
          apiResponse.mapApiRequestResults { "No results found." }
        }

      emitAll(placesApiResponse)
    }

  private fun filterResult(
    placeAddress: String
  ): Flow<ApiResponse<PlaceAutoCompleteResponse>> =
    flow {
      val searchResult = getResult {
        apiService.getAutocomplete(placeAddress)
      }
      emit(searchResult)
    }

  override fun getPlaceDetails(
    placeId: String
  ): Flow<Result<PlaceDetailsResponse>> =
    flow {
      emit(Result.Loading)

      val apiResponse = getResult {
        apiService.getPlaceById(placeId)
      }.mapApiRequestResults { "Invalid request type." }

      emit(apiResponse)
    }
}

