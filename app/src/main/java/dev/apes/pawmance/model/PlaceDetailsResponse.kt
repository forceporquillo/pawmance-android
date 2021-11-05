package dev.apes.pawmance.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import dev.apes.pawmance.api.ApiResponseStatus

data class PlaceDetailsResponse(
  @SerializedName("result")
  @Expose
  val result: Result? = null,

  @SerializedName("status")
  @Expose
  override val status: String? = null
) : ApiResponseStatus

data class Result(
  @SerializedName("formatted_address")
  @Expose
  var formattedAddress: String? = null,

  @SerializedName("geometry")
  @Expose
  var geometry: Geometry? = null,

  @SerializedName("name")
  @Expose
  var name: String? = null,

  @SerializedName("place_id")
  @Expose
  var placeId: String? = null,

  @SerializedName("reference")
  @Expose
  var reference: String? = null
)

data class Geometry(

  @field:SerializedName("location")
  val location: Location? = null,
)

data class Location(

  @field:SerializedName("lng")
  val lng: Double? = null,

  @field:SerializedName("lat")
  val lat: Double? = null
)