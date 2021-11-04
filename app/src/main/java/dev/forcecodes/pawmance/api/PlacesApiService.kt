package dev.forcecodes.pawmance.api

import com.devforcecodes.pawmance.BuildConfig
import dev.forcecodes.pawmance.model.PlaceAutoCompleteResponse
import dev.forcecodes.pawmance.model.PlaceDetailsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {

  @GET("/maps/api/place/autocomplete/json")
  suspend fun getAutocomplete(
    @Query("input") input: String?,
    @Query("components") component: String = "country:ph",
    @Query("radius") radius: String = "5000",
    @Query("key") apiKey: String = BuildConfig.MAPS_API_KEY
  ): Response<PlaceAutoCompleteResponse>

  @GET("/maps/api/place/details/json")
  suspend fun getPlaceById(
    @Query("place_id") id: String?,
    @Query("key") apiKey: String = BuildConfig.MAPS_API_KEY
  ): Response<PlaceDetailsResponse>
}

