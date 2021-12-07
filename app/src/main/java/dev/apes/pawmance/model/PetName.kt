package dev.apes.pawmance.model

data class PetName(
  val id: String,
  val name: String,
  val breed: String? = null,
  val birthdate: String? = null,
  val preference: String? = null,
  val gender: String? = null
)