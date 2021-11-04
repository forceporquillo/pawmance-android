package dev.forcecodes.pawmance.model

data class MyLocation @JvmOverloads constructor(
  val placeId: String? = null,
  val coordinates: Coordinates? = null,
  val placeName: String? = ""
)

data class Coordinates(
  val lat: Double? = 0.0,
  val lng: Double? = 0.0
)
